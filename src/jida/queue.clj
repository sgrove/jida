(ns jida.queue)

(defn queue-repo-import! [repo-address]
  (println "Asked to import repo: " repo-address)
  (println "Currently a NO-OP")
  (rand-nth [true false]))
