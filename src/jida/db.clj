(ns jida.db
  (:require [carica.core :as carica]
            [clojure.java.jdbc :as sql]
            [clojure.string :as string]
            [clojure.tools.reader.edn :as edn]
            [korma.core :as korm]
            [korma.db :as db])
   (:import (java.net URI)
            (java.util.UUID)))

(defn uri->db-spec [mode uri]
  (let [db-uri (java.net.URI. uri)]
    (merge {:db (last (string/split uri #"\/"))
            :host (.getHost db-uri)
            :port (.getPort db-uri)}
           (when (= mode :prod)
             {:ssl true
              :sslfactory (when (= mode :dev) "org.postgresql.ssl.NonValidatingFactory")})
           (when-let [user-info (.getUserInfo db-uri)]
             (let [[user password] (string/split user-info #":")]
               {:user user
                :password password})))))

(defn mode []
  (carica/config :env-name))

(defn pg-db [mode uri]
  (db/postgres (uri->db-spec mode uri)))

(def local-pg-string "postgres://localhost:15432/jida_dev")
(def heroku-pg-string (System/getenv "DATABASE_URL"))

(defn db-spec [mode]
  (pg-db mode (or local-pg-string heroku-pg-string)))

(defn connect! []
  (db/defdb default (db/postgres (db-spec (mode)))))

(defn create-queries-table []
  (sql/with-connection db-spec
    (sql/create-table
     :queries
     [:id "serial"]
     [:uuid "varchar(255) not null"]
     [:title "varchar(255)"]
     [:description "text"]
     [:query_body "text not null"]
     [:args "text"]
     [:created_at "timestamp default statement_timestamp()"])))

;;******************************************************************************
;;  Helpers
;;******************************************************************************

(defn uuid []
  (.toString (java.util.UUID/randomUUID)))

(defn now []
  (java.sql.Timestamp. (.getTime (java.util.Date.))))

;;******************************************************************************
;;  Initial entity declarations
;;******************************************************************************

(korm/defentity queries
  (korm/database default))
