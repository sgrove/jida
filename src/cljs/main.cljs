(ns jida.client.main
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer put! close!]]
            [cljs.reader :as reader]
            [clojure.browser.repl :as repl]
            [clojure.string :as string]
            [dommy.core :as dommy]
            [goog.Uri :as uri]
            [goog.dom.selection :as selection]
            [jida.client.api :as api]
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

(defn main [target controls-ch data-ch log-ch stop-ch]
  (ui/build! target)
  (ui/ui->chans! target controls-ch)
  (ui/select! :query)
  (go (loop []
        (alt!
         stop-ch ([v]
                    (utils/log "Got stop chan, killing: " (pr-str v))
                    (utils/log "Destroying ui target: " target)
                    (ui/destroy! target))
         controls-ch ([v] (let [[message data] v]
                            (utils/log "controls start: " message ", " v)
                            (match message
                                   :query-selected (ui/select! :query)
                                   :repos-selected (ui/select! :repos)
                                   :info-selected  (ui/select! :info)
                                   :user-query-updated  (check-query-parens! target data false)
                                   :query-save     (when (check-query-parens! target data true)
                                                     (api/save-query! data nil nil))
                                   :query-submit   (when (check-query-parens! target data true)
                                                     (api/run-query! data))
                                   :import-repo    (when (check-valid-git-url! target data)
                                                     (ui/set-repo-url-message-status! target (str "Importing " data "..."))
                                                     (api/import-repo! data))
                                   (utils/log "Unrecognized control message" ))
                            (utils/log "end")
                            (recur)))
         data-ch ([v]
                    (let [[message data] v]
                      (utils/log "data start: " message ", " data)
                      (match message
                             :query-updated (ui/update-query! target data)
                             :query-history-updated (ui/update-query-history! target data)
                             :available-repos-updated (ui/update-available-repos! target data)
                             :results-updated (do (utils/log "Ok, results updated")
                                                  (let [[_ results query] v]
                                                    (utils/log "Results: " results)
                                                    (utils/log "Query: " query)
                                                    (ui/display-results! target results query)))
                             :else (utils/log "Unrecognized data message"))
                      (utils/log "Data channel: " v)
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
  ;; TODO: Load example query
  ;; TODO: Retrieve given query and run it, displaying the results
  (when utils/initial-query-id
    (utils/log "Loading existing query for " utils/initial-query-id))

  ;(ui/update-query-history!)
  (main :#wrapper controls-ch data-ch log-ch main-stop-ch))

(set! (.-onload js/window) setup!)
