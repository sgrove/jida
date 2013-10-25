(ns jida.client.ui
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer put! close!]]
            [cljs.reader :as reader]
            [clojure.string :as string]
            [dommy.core :as dommy]
            [goog.Uri :as uri]
            [goog.dom.selection :as selection]
            [jida.client.utils :as utils]
            [jida.client.views :as views])
  (:require-macros [cljs.core.async.macros :as am :refer [go alt!]]
                   [cljs.core.match.macros :refer [match]])
  (:use-macros [dommy.macros :only [node sel sel1]]))

(defn safe-read [s]
  (reader/read-string s))

(defn extract-find-args [query]
  (let [q (safe-read query)]
    (map str
         (take-while #(not (keyword? %))
                     (drop 1 (drop-while #(not (= :find %)) q))))))

(defn select-character [text-area offset]
  (selection/setStart text-area offset)
  (selection/setEnd text-area (inc offset)))

(defn display-results! [parent results query]
  {:pre [(or (vector? results) (map? results))]}
  (utils/log query (extract-find-args query))
  (let [results-el (sel1 [parent :#results])]
    (dommy/clear! results-el)
    (-> results-el
        (dommy/append! (if (:error results)
                        (views/results-error results)
                        (views/results-items results (extract-find-args query))))
     (dommy/show!))))

(defn display-error! [message]
  (-> (sel1 :#error-messages)
      (dommy/set-html! message)
      dommy/show!))

(defn set-query-field [query]
  (dommy/set-text! (sel1 :#query-text) query))

(defn set-query-title! [title]
  (dommy/set-text! (sel1 :#query-title) title))

(defn set-query-description! [description]
  (dommy/set-text! (sel1 :#description) description))

(defn check-save-button []
  (utils/log "Checking the save button")
  (let [query-node (sel1 :#query-text)
        query-value (dommy/value query-node)]
    (if (= (count query-value) 0)
      (dommy/set-attr! (sel1 :#query-save) :disabled true)
      (dommy/remove-attr! (sel1 :#query-save) :disabled))))

(defn update-query-history! [parent history]
  (let [history-el (sel1 [parent :#query-history])
        content (views/query-history history)]
    (dommy/clear! history-el)
    (dommy/append! history-el content)))

(defn update-query! [parent query]
  (-> (sel1 [parent :#query-text])
      (dommy/set-value! query)))

(defn update-available-repos! [parent repos]
  (let [repos-el (sel1 [parent :#available-repos])
        content (views/repos repos)]
    (dommy/clear! repos-el)
    (dommy/append! repos-el content)))

(defn disable-tab-el! [el]
  (dommy/remove-class! el "active"))

(defn select! [tab-name]
  (doseq [tab (sel :.tab-pane)]
    (disable-tab-el! tab))
  (-> (sel1 (str "#" (name tab-name) ".tab-pane"))
      (dommy/add-class! "active")))

(defn tabs->chans! [parent tab-ids target-ch]
  (doseq [tab-id tab-ids]
    (dommy/listen! (sel1 [parent (str "#" (name tab-id))]) :click (fn [data]
                                                           (utils/log "Got an event: " :click " on target " tab-id)
                                                           (utils/relay target-ch (keyword (str (name tab-id) "-selected")) data)
                                                          false))))

(defn repo->chans! [parent target-ch]
  (let [repo-import-input (sel1 [parent :#repo-address])
        repo-import-button (sel1 [parent :#import-repo-btn])
        get-value (fn [el] (.-value el))]
    (dommy/listen! repo-import-input :keypress (fn [event]
                                                 (when (= 13 (.-keyCode event))
                                                   (utils/relay target-ch :import-repo (get-value repo-import-input)))))
    (dommy/listen! repo-import-button :click (fn [event]
                                               (utils/relay target-ch :import-repo (get-value repo-import-input))))))

(defn ui->chans! [parent target-ch]
  (tabs->chans! parent [:query :repos :info] target-ch)
  (repo->chans! parent target-ch)
  (let [text-el (sel1 [parent :#query-text])]
    (let [targets [[:query-submit    [:click]]
                   [:query-save      [:click]]]]
      (doseq [[target events] targets]
        (doseq [event events]
          (dommy/listen! (sel1 (str "#" (name target))) event (fn [data]
                                                                (utils/log "Got an event: " event " on target " target)
                                                                (utils/relay target-ch target (.-value text-el))
                                                                false)))))
    
    (doseq [event-type [:keyup :blur :onchange]]
      (dommy/listen! text-el event-type (fn [event]
                                          (utils/relay target-ch :user-query-updated (.-value text-el)))))))

(defn mark-paren-errors! [target errors & [select-error?]]
  (utils/log "Select error? " select-error?)
  (when select-error?
    (-> (sel1 [target :#query-text])
        (select-character (first errors))))
  (-> (sel1 [target :#error-messages])
      (dommy/set-text! (str "Unbalanced parens at positions" (string/join ", " errors)))
      (dommy/show!)))

(defn clear-paren-errors! [target]
  (-> (sel1 [target :#error-messages])
      (dommy/hide!)))

(defn set-repo-url-message-status! [target error]
  (-> (sel1 [target :#import-status])
      (dommy/set-text! error)
      (dommy/show!)))

(defn clear-repo-url-message-status! [target error]
  (-> (sel1 [target :#import-status])
      (dommy/set-text! "")
      (dommy/hide!)))

(defn build! [target]
  (-> (sel1 target)
      (dommy/append! (views/base))))

(defn destroy! [target]
  (-> (sel1 target)
      (dommy/clear!)))
