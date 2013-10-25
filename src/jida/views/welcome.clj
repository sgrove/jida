(ns jida.views.welcome
  (:require [jida.views.common :as common]
            [jida.datomic :as jida])
  (:use [hiccup.core :refer :all]))

(def home*
  (common/layout))

(def home (html home*))
