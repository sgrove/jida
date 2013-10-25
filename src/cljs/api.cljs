(ns jida.client.api
  (:require [jida.client.utils :as utils]))

(defn save-query! [title description query]
  (utils/log "TODO: Save query: " title "," description ":" query))

(defn run-query! [query]
  (utils/log "TODO: Run query: " query))

(defn import-repo! [repo-address]
  (utils/log "TODO: Import repo at: " repo-address))
