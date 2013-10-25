(ns jida.server
  (:use [compojure.core]
        [ring.adapter.jetty])
  (:require [cemerick.piggieback]
            [cljs.repl.browser]
            [jida.datomic :as jida]
            [jida.queue :as jiqu]
            [jida.queries :as queries]
            [jida.routes.api :as api-routes]
            [jida.routes.content :as content-routes]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.response :as response]))

; Ideas
; 1. Function-specific queries: explore functions
;   a. Function history: Most frequent commit author ("Who changed this function most?")
;   b. Calculate code complexity for a function over time,
;        sort by current most complex functions,
;        who contributed most complexity,
;        when?
; 2. Repo-wide queries: explore projects
; 3. Author-specific queries: explore contributor history
; 4. Import repos
; 5. Allow me to save/share/fork queries I think are useful
; 6. Related to 5, take queries from url so we can have pastie-like sharing

(defonce conn (atom nil))

(def app-routes
  (routes api-routes/api-routes
          content-routes/content-routes))

(def app
  (-> (handler/site app-routes)))

(defonce servers (atom {}))

(defn create-server! [port background? & [ssl?]]
  (println (str "Creating server on port " port (when background? " in the background") " with " app))
  (run-jetty (var app) {:port port :join? (not background?)}))

(defn stop! [name]
  (.stop (get @servers name)))

(defn restart! [name]
  (.stop  (get @servers name))
  (.start (get @servers name)))

(defn quick-start! [& [port background? ssl?]]
  (swap! servers assoc :default (create-server! (or port 3000) (or background? true) ssl?)))

(defn start-named! [name & [port background? ssl?]]
  (swap! servers assoc name (create-server! (or port 3000) (or background? true) ssl?)))

(defn -main [& args]
  (reset! conn (jida/connect))
  (start-named! :command-line (or (when-let [port (first args)] (Integer/parseInt port)) 3000) false false))

;;******************************************************************************
;;  ClojureScript Helpers
;;******************************************************************************

(defn start-cljs-repl []
   (cemerick.piggieback/cljs-repl
  :repl-env (doto (cljs.repl.browser/repl-env :port 9000)
              cljs.repl/-setup)))

(def repl-env (reset! cemerick.austin.repls/browser-repl-env
                      (cemerick.austin/repl-env)))

;(start-named! :auto-start 3000 true false)

;; (defremote query-codeq [q]
;;   (println "Received query for" q)
;;   (let [result (try (jida/query q @conn)
;;                  (catch Exception e {:error (str e)}))]
;;     (println)
;;     (println "Finished: " result)
;;     result))

;; (defremote queue-import [address]
;;   (jiqu/queue-import address))
