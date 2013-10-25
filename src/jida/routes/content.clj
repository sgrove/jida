(ns jida.routes.content
  (:use [compojure.core]
        [ring.adapter.jetty])
  (:require [jida.views.welcome :as welcome-views]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.response :as response]))

(defroutes content-routes
  (GET "/" [] welcome-views/home)
  (route/resources "/"))
