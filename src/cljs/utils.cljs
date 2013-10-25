(ns jida.client.utils
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer put! close!]]
            [goog.Uri])
  (:require-macros [cljs.core.async.macros :as am :refer [go alt!]]
                   [cljs.core.match.macros :refer [match]]))

(defn https-url? [url]
  (= (subs url 0 8) "https://"))

(defn valid-git-url? [url]
  (https-url? url))

(def parsed-uri
  (goog.Uri. (-> (.-location js/window) (.-href))))

(def initial-query-id
  (.getParameterValue parsed-uri "query-uuid"))

(defn log [& msg]
  (.apply (.-log js/console) js/console (clj->js msg)))

(defn relay [ch message data]
  (go (>! ch [message data])))
