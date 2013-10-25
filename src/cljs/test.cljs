(ns jida.client.test
  (:use-macros [dommy.macros :only [node sel sel1]]))

(defn testfn []
  (sel :.test))

(defn testfn2 []
  (sel :.hello))
