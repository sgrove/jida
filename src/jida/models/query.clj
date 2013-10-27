(ns jida.models.query
  (:require [clojure.set :as sets]
            [clojure.tools.reader.edn :as edn]
            [jida.db :as db]
            [korma.core :as korm]))

(defn valid-edn? [string]
  (try (edn/read-string string) true
       (catch Exception e false)))

(defn find-all []
  (korm/select db/queries))

(defn by-id [id]
  (first (korm/select db/queries
                      (korm/where (= :id id)))))

(defn by-uuid [uuid]
  (first (korm/select db/queries
                      (korm/where (= :uuid uuid)))))

(defn save [query-map]
  (let [required-keys #{:query_body}]
    (cond
     (or (not (sets/subset? required-keys
                            (set (keys query-map))))
         (some empty? (map query-map required-keys))) [false "Query cannot be blank"]
         (not (valid-edn? (:query_body query-map))) [false "Query must be valid edn"]
         (let [form (edn/read-string (:query_body query-map))]
           (not (vector? form))) [false "Query top-level must be one vector"]
         :else [(korm/insert db/queries (korm/values (merge {:uuid (db/uuid)
                                                             :created_at (db/now)}
                                                            query-map)))
                "Query saved successfully"])))

(defn recent [as-of & [limit]]
  (korm/select db/queries
               (korm/limit (or limit 5))
               (korm/order :created_at :DESC)
               (korm/where (< :created_at as-of))))
