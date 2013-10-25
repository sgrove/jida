(ns jida.client.views
  (:require [dommy.core :as dommy])
  (:use-macros [dommy.macros :only [node sel sel1]]))

(def query-content*
  [:div#query.tab-pane
   [:div.row
    [:div.span8
     [:p
      [:strong#query-title]
      [:span#description]]
     [:textarea#query-text
      {:rows 3
       :placeholder "Your query"}
      "[:find ?repo-names :where [?repos :repo/uri ?repo-names]]"]
     [:div.controls
      [:input#query-save.btn.btn-large.btn-safe {:value "save" :type "submit"}]
      [:input#query-submit.btn.btn-large.btn-primary {:value "run" :type "submit"}]
      [:img#loader {:style "display:none;" :src "https://www.zenboxapp.com/assets/loading.gif"}]
      [:div#error-messages.alert {:style "display:none;"}]]]
    [:div.span3.sidebar
     [:div
      [:label "Recent saved queries"]
      [:div#query-history]]]]
   [:div.row
    [:div.span11
     [:p#results]]]])

(def repos-content*
  [:div#repos.tab-pane
   [:div.repos
    [:p "Available repos: "
     [:div#available-repos]]]
   [:div
    [:label "Add your repo"]
    [:input#repo-address.input-xlarge
     {:type "text"
      :value "https://github.com/clojure/clojure.git"}]
    [:input#import-repo-btn.btn.btn-small
     {:value "import"
      :type "submit"}]
    [:div#import-status.alert.alert-info {:style "display:none;"} ""]]])

(def info-content*
  [:div#info.tab-pane
   [:p "Some relevant links:"]
   [:ul.schema
    [:li
     [:a {:target "_blank" :href "http://cloud.github.com/downloads/Datomic/codeq/codeq.pdf"} "Codeq schema"]]
    [:li
     [:a {:target "_blank" :href "http://docs.datomic.com/tutorial.html"} "Datomic query tutorial"]]
    [:li
     [:a {:target "_blank" :href "https://github.com/devn/codeq-playground/blob/master/src/com/thinkslate/codeq_playground/core.clj"} "Useful example queries"]]
    [:li
     [:a {:target "_blank" :href "https://github.com/yayitswei/jida"} "Jida Source"]]]])

(def log-container*
  [:div#log-container
   [:ul#log
    [:li "Log output"]]])

(defn tab* [[id content]]
  [:li [:a {:id id
            :href (str "#" id)
            :data-toggle "tab"} content]])

(def base*
  (list
   [:h1 "Jida - Explore Clojure Projects"]
   [:ul#nav.nav.nav-tabs
    (map tab* [["query" "Query"]
               ["repos" "Repos"]
               ["info" "Getting started"]])]
   [:div.tab-content
    query-content*
    repos-content*
    info-content*
    log-container*]))

(defn base []
  (node base*))

(defn results-item* [fields]
  [:tr (map #(vector :td %) fields)])

(defn results-items* [items headers]
  [:div.results
   [:p (count items) " returned."]
   [:table.table.table-bordered.table-striped
    [:thead
     [:tr (map #(vector :th %) headers)]]
    [:tbody (map results-item* items)]]])

(defn results-items [items headers]
  (node (results-items* items headers)))

(defn results-error* [error]
  [:div.results.alert.alert-error (:message error)])

(defn results-error [arg]
  (node (results-error* arg)))

(defn repo* [repo]
  [:a {:href repo :target "_blank"} repo])

(defn repos* [repos]
  [:div (interpose ", " (map repo* repos))])

(defn repos [repos]
  (node (repos* repos)))

(defn friendly-title [title]
  (if (empty? title) "untitled" title))

(defn friendly-description [description]
  (if (empty? description) "" (str ": " description)))

(defn query-link [id]
  (str "/?query-id=" id))

(defn query-history-item* [item]
  (println "Item: " item)
  (let [{:keys [title description _id]} item]
    [:li
     [:a {:href (query-link _id)
          :target "_blank"} (friendly-title title)]
     (friendly-description description)]))

(defn query-history-item [arg]
  (node (query-history-item* arg)))

(defn query-history* [items]
  [:ul (map query-history-item* items)])

(defn query-history [items]
  (node (query-history* items)))
