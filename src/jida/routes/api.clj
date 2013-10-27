(ns jida.routes.api
  (:use [compojure.core])
  (:require [clojure.tools.reader.edn :as edn]
            [compojure.route :as route]
            [compojure.response :as response]
            [jida.datomic :as jida]
            [jida.db :as db]
            [jida.models.repo :as repo]
            [jida.models.query :as query]
            [jida.queue :as queue]
            [jida.support.cors :as cors]
            [ring.middleware.json :as ring-json]
            [ring.middleware.params :as ring-params]))

(defroutes cors-routes
  (OPTIONS "/queries/recent.json" []
           {:headers cors/headers
            :body "OK"})
  (OPTIONS "/queries/:uuid/results.json" []
           {:headers cors/headers
            :body "OK"})
  (OPTIONS "/repos/available.json" []
           {:headers cors/headers
            :body "OK"}))

(defroutes api-routes*
  (GET "/queries/recent.json" [limit]
    {:status 200
     :body {:recently-saved-queries (query/recent (db/now) (or limit 5))}})
  (GET "/queries/:uuid/results.json" [uuid]
    (let [query (query/by-uuid uuid)]
      {:status (if query 200 404)
       :body (if query
               {:success true
                :query query
                :results (jida/query (edn/read-string (:query_body query)) jida/connection)}
               {:success false
                :message "No query with that uuid"})}))
  (GET "/queries/:uuid.json" [uuid]
    (let [query (query/by-uuid uuid)]
      {:status (if query 200 404)
       :body (if query
               {:query query}
               {:message "No query with that uuid"})}))  
  (GET "/query.json" [query]
    (println "Query body: " query)
    {:status 200
     :body (jida/query (edn/read-string query) jida/connection)})
  (POST "/query.json" [title description query]
    (let [[success message] (query/save {:title title
                                         :description description
                                         :query_body query})
          status (if success 200 426)]
      {:status status
       :body  {:success (boolean success)
               :message message}}))
  (PUT "/repos/import.json" [repo-address]
    (let [success (queue/queue-repo-import! repo-address)]
      {:status (if success 200 426)
       :body {:success success
              :message "Repo queued for import"}}))
  (GET "/repos/available.json" []
    (let [repos (repo/available jida/connection)]
      {:status 200
       :body {:repos repos}})))

(def all-routes*
  (routes cors-routes
          (var api-routes*)))

(def api-routes
  (-> (var all-routes*)
      (ring-json/wrap-json-body)
      (ring-json/wrap-json-params)
      (ring-params/wrap-params)
      (ring-json/wrap-json-response)))
