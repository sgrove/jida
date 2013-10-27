(ns jida.client.api.callbacks
  (:require [ajax.core :as ajax]
            [jida.client.utils :as utils]))

(def request-type "json")

(defn find-query [uuid cb]
  (ajax/ajax-request (str "/queries/" uuid "." request-type) :get
                     {:handler cb
                      :format (ajax/json-response-format {:keywords? true})}))

(defn run-query! [query cb]
  (ajax/ajax-request (str "/query." request-type) :get
                     {:params {:query query}
                      :handler cb
                      :format (ajax/json-response-format {:keywords? true})}))

(defn save-query! [title description query cb]
  (let [request-type "json"]
    (ajax/ajax-request (str "/query." request-type) :post
                       {:params {:query query
                                 :title title
                                 :description description}
                        :handler cb
                        :format (ajax/json-format {:keywords? true})})))

(defn import-repo! [repo-address cb]
  (ajax/ajax-request (str "/repos/import." request-type) :put
                     {:params {:repo-address repo-address}
                      :handler cb
                      :format (ajax/json-format {:keywords? true})}))

(defn fetch-recent-saved-queries [limit cb]
  (ajax/ajax-request (str "/queries/recent." request-type) :get
                     {:params {:limit limit}
                      :handler cb
                      :format (ajax/json-response-format {:keywords? true})}))

(defn fetch-available-repos [cb]
  (ajax/ajax-request "/repos/available.json" :get
                     {:handler cb
                      :format (ajax/json-response-format {:keywords? true})}))
