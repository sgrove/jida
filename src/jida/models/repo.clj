(ns jida.models.repo
  (:require [jida.datomic :as jida]))

(def available-query
  '[:find ?repo-names :where [?repos :repo/uri ?repo-names]])

(defn available [conn]
  (map first (jida/query available-query conn)))
