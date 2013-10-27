(ns jida.client.main
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer put! close!]]
            [cljs.reader :as reader]
            [clojure.browser.repl :as repl]
            [clojure.string :as string]
            [dommy.core :as dommy]
            [goog.Uri :as uri]
            [goog.dom.selection :as selection]
            [jida.client.api.channels :as api]
            [jida.client.parens :as parens]
            [jida.client.ui :as ui]
            [jida.client.utils :as utils]
            [jida.client.views :as views])
  (:require-macros [cljs.core.async.macros :as am :refer [go alt!]]
                   [cljs.core.match.macros :refer [match]])
  (:use-macros [dommy.macros :only [node sel sel1]]))


;;******************************************************************************
;;  Logging
;;******************************************************************************

(defn log-node [message & [data]]
  [:li.log-message message
   (when data
     [:span.toggle ": " [:span.data (pr-str data)]])])

(def counter (atom 0))

(def max-log-length 45)

(defn log! [message & [data]]
  (swap! counter inc)
  (let [el (sel1 :#log)
        log-el (node (log-node (str @counter ". " message) data))]
    (dommy/prepend! el log-el))
  ; Limit to the last n 
  (let [log-nodes (sel [:#log :li])]
    (doseq [node (drop max-log-length log-nodes)]
      (dommy/clear! node)
      (dommy/remove! node))))

(defn enable-logging! [log-ch]
  (when (sel1 :#log-container)))

;;******************************************************************************
;;  Main UI Loop
;;******************************************************************************

(defn check-query-parens! [target data & [select?]]
  (utils/log "Here? Select? " select?)
  (let [[balanced? errors] (parens/balanced-parens? data)]
    (if balanced?
      (ui/clear-paren-errors! target)
      (ui/mark-paren-errors! target errors select?))
    (utils/log "Now Here?")
    balanced?))

(defn check-valid-git-url! [target url]
  (if (utils/valid-git-url? url)
    (ui/clear-repo-url-message-status! target)
    (ui/set-repo-url-message-status! target "This is not a valid (public) git url"))
  (utils/valid-git-url? url))

(defn show-status-for! [target seconds message]
  (go (ui/set-status-message! target message)
      (<! (async/timeout (* 1000 seconds)))
      (ui/clear-status-message! target)))

; TODO: Add a universal error queueing function
(defn show-error-for! [target seconds message]
  (go (ui/set-error! target message)
      (<! (async/timeout (* 1000 seconds)))
      (ui/clear-error! target)))

(defn update-query-history! [target data-ch]
  (go (let [update-history-ch (chan)]
        (api/fetch-recent-saved-queries 5 update-history-ch)
        (let [[message [data response]] (<! update-history-ch)]
          (utils/log "Fetch history response: " response)
          (condp = message
            :fetch-recent-saved-queries-succeeded (put! data-ch [:query-history-updated (get-in response [:recently-saved-queries])])
            (show-error-for! target 5 "Error fetching recent query history"))))))

(defn update-available-repos! [target data-ch]
  (go (let [update-repos-ch (chan)]
        (api/fetch-available-repos update-repos-ch)
        (let [[message [data response]] (<! update-repos-ch)]
          (utils/log "Fetch available repos response: " response)
          (utils/log "Message:" message)
          (utils/log "Data:" data)
          (if (= message :fetch-available-repos-succeeded)
            (put! data-ch [:available-repos-updated (get-in response [:repos])])
            (show-error-for! target 5 "Error fetching available repos"))))))

(defn main [target controls-ch data-ch log-ch stop-ch]
  (ui/build! target)
  (ui/ui->chans! target controls-ch)
  (ui/select! :query)
  (update-query-history! target data-ch)
  (update-available-repos! target data-ch)
  (when utils/initial-query-id
    (let [initial-query-ch (chan)]
      (go (api/find-query utils/initial-query-id initial-query-ch)
          (let [[message [data response]] (<! initial-query-ch)]
            (if (= message :find-query-succeeded)
              (put! data-ch [:query-updated (:query response)])
              (show-error-for! target 5 (str "Error retrieving query " utils/initial-query-id)))))))
  (go (loop []
        (alt!
         stop-ch ([v]
                    (ui/destroy! target))
         controls-ch ([v] (let [[message data] v]
                            (match message
                                   :query-selected (ui/select! :query)
                                   :repos-selected (ui/select! :repos)
                                   :info-selected  (ui/select! :info)
                                   :user-query-updated  (check-query-parens! target data false)
                                   :query-save     (when (check-query-parens! target data true)
                                                     (ui/enable-spinner! target)
                                                     (let [save-ch (chan)
                                                           title (js/prompt "Query title")
                                                           description (js/prompt "Query description")
                                                           ]
                                                       (api/save-query! title description data save-ch)
                                                       (let [[message [data response]] (<! save-ch)]
                                                         (ui/disable-spinner! target)
                                                         (show-status-for! 5 target "Query saved!")
                                                         (condp = message
                                                           :save-query-succeeded (update-query-history! target data-ch)
                                                           
                                                           (do
                                                             (ui/set-error! target (str "There was an error saving your query: " (get-in response [:response :message]))))))))
                                   :query-submit   (when (check-query-parens! target data true)
                                                     (ui/enable-spinner! target)
                                                     (let [run-ch (chan)]
                                                       (api/run-query! data run-ch)
                                                       (let [[message [data response]] (<! run-ch)]
                                                         (ui/disable-spinner! target)
                                                         (condp = message
                                                           :run-query-succeeded (put! data-ch [:results-updated
                                                                                               response
                                                                                               (:query data)])
                                                           (do
                                                             (ui/clear-results! target)
                                                             (ui/set-error! target "There was an error running your query."))))))
                                   :import-repo    (when (check-valid-git-url! target data)
                                                     (ui/set-repo-url-message-status! target (str "Queueing " data " for import..."))
                                                     (let [import-ch (chan)]
                                                       (api/import-repo! data import-ch)
                                                       (let [[message [data response]] (<! import-ch)]
                                                         (condp = message
                                                           :import-repo-succeeded (ui/set-repo-url-message-status! target (str (:repo-address data) " successfully queued for import"))
                                                           (ui/set-repo-url-message-status! target (str "Error queueing " (:repo-address data) " for import")))))))
                            (recur)))
         data-ch ([v]
                    (let [[message data] v]
                      (utils/log "DAta ch: " message ", " data ", " v)
                      (condp = message
                             :query-updated (ui/update-query! target data)
                             :query-history-updated (ui/update-query-history! target data)
                             :available-repos-updated (ui/update-available-repos! target data)
                             :results-updated (do (let [[_ results query] v]
                                                     (ui/display-results! target results query)))
                              (utils/log "Unrecognized data message"))
                      (recur)))
         log-ch ([[message data]]
                   (log! message data)
                   (recur))))))

(def controls-ch (chan))
(def log-ch (chan))
(def main-stop-ch (chan))
(def data-ch (chan))

(comment
  (main :#wrapper controls-ch data-ch log-ch main-stop-ch)
  (go (>! main-stop-ch "Please stop"))
  (ui/update-query! :#wrapper {:title "Test" :description "D" :query_body "ABC"})
  (put! data-ch [:results-updated
                 (vec (repeatedly (rand-int 15) #(rand-nth [["sean" (rand-int 99)] ["marissa" (rand-int 99)] ["diego" (rand-int 99)]])))
                 "[:find ?repo-names ?age :where [?repos :repo/uri ?repo-names]]"])
  (go (>! data-ch [:results-updated
                   [["sean" 28] ["marissa" 24] ["diego" 42]]
                   "[:find ?repo-names ?age :where [?repos :repo/uri ?repo-names]]"]))
  (go (>! data-ch [:results-updated
                   {:error true
                    :message "Display an error"}
                   "[:find ?repo-names ?age :where [?repos :repo/uri ?repo-names]]"]))
  (go (>! data-ch [:query-updated "Please stop SSSOOO NICE!"]))
  (go (>! data-ch [:available-repos-updated ["Please stop SSSOOO NICE!"  "And me too!"]]))
  (go (>! data-ch [:query-history-updated [{:title "Example query" :description "Some description for you" :_id :abc123}
                                           {:title "Example query" :description "Some description for you" :_id :abc123}
                                           {:title "Example query" :description "Some description for you" :_id :abc123}
                                           {:title "Example query" :description "Some SDFA for you" :_id :abc123}]]))
  (go (>! log-ch (<! controls-ch)))
  (go (>! log-ch ["Log this!" {:place "holder"}])))

(defn ^:export setup! []
  (main :#wrapper controls-ch data-ch log-ch main-stop-ch))

(set! (.-onload js/window) setup!)
