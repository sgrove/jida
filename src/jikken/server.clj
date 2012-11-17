(ns jikken.server
  (:require [noir.server :as server]
            [jikken.datomic :as jida])
  (:use [noir.fetch.remotes]))

(server/load-views-ns 'jikken.views)

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'jikken})))

(defremote query-codeq [q]
  (println "Received query for" q)
  (let [result (jida/query q)]
    (println "Finished!")
    result))
