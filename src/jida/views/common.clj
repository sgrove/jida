(ns jida.views.common
  (:require [cemerick.austin.repls :refer [browser-connected-repl-js]])
  (:use [hiccup.core :refer :all]))

(defn layout [& content]
  [:html
   [:head
    [:title "Jida - Explore your Clojure projects"]
    [:link {:rel "stylesheet", :href "/css/bootstrap.simplex.min.css"}]
    [:link {:rel "stylesheet", :href "/css/styles.css"}]]
   [:body
    [:div#wrapper.container
     content]
    [:script {:type "text/javascript" :src "/js/bin-debug/main.js"}]
    [:script (browser-connected-repl-js)]]])
