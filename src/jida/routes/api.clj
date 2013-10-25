(ns jida.routes.api
  (:use [compojure.core]
        [ring.adapter.jetty])
  (:require [jida.datomic :as jida]
            [jida.queue :as jiqu]
            [jida.queries :as queries]
            [jida.views.welcome :as content]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.response :as response]
            [ring.middleware.json :as middleware]
            [ring.middleware.params :as params]))

(defroutes api-routes*
  (GET "/test.json" [] {:status 200
                        :body {:test "OK?"}}))

(def api-routes
  (-> api-routes*
      (middleware/wrap-json-body)
      (middleware/wrap-json-params)
      (params/wrap-params)
      (middleware/wrap-json-response)))
