(ns jida.client.api.channels
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer put! close!]]
            [jida.client.api.callbacks :as cb]
            [jida.client.utils :as utils])
   (:require-macros [cljs.core.async.macros :as am :refer [go alt!]]
                    [cljs.core.match.macros :refer [match]]))

(defn handler [ch message data]
  (fn [[ok? response]]
    (utils/log "Response handler for " (pr-str message) " : " ok?)
    (if ok?
      (utils/relay ch (keyword (str (name message) "-succeeded")) [data response])
      (utils/relay ch (keyword (str (name message) "-failed")) [data response]))))

(defn find-query [uuid ch]
  (cb/find-query uuid (handler ch :find-query {:uuid uuid})))

(defn save-query! [title description query ch]
  (cb/save-query! title description query (handler ch :save-query {:title title
                                                                   :description description
                                                                   :query query})))

(defn run-query! [query ch]
  (cb/run-query! query (handler ch :run-query {:query query})))

(defn import-repo! [repo-address ch]
  (cb/import-repo! repo-address (handler ch :import-repo {:repo-address repo-address})))

(defn fetch-recent-saved-queries [limit ch]
  (cb/fetch-recent-saved-queries limit (handler ch :fetch-recent-saved-queries {:limit limit})))

(defn fetch-available-repos [ch]
  (cb/fetch-available-repos (handler ch :fetch-available-repos nil)))
