(ns publicator.web.post.list.view
  (:require
   [hiccup.core :refer [html]]
   [io.pedestal.http.route :as route]))

(defn- render-post [post]
  (html
   [:tr
    [:th {:scope :row} (:id post)]
    [:td (:title post)]
    [:td (-> post :author :full-name)]]))

(defn render [posts]
  (html
   [:table.table
    [:thead
     [:tr
      [:th {:scope :col} "#"]
      [:th {:scope :col} "Title"]
      [:th {:scope :col} "Author"]]]
    [:tbody
     (map render-post posts)]]))
