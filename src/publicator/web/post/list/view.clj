(ns publicator.web.post.list.view
  (:require
   [hiccup.core :refer [html]]
   [io.pedestal.http.route :as route]
   [publicator.interactors.services.user-session :as user-session]
   [publicator.web.helpers :as h]))

(defn- render-post [post]
  (html
   [:tr
    [:th {:scope :row} (:id post)]
    [:td
     (h/link-to (:title post)
                (route/url-for :post-show
                               :path-params {:id (:id post)}))]
    [:td (-> post :author :full-name)]]))

(defn render [posts]
  (html
   [:div
    (if (user-session/logged-in?)
      (h/link-to "New post" (route/url-for :post-create-form)
                 :class "btn btn-primary my-3"))
    [:table.table
     [:thead
      [:tr
       [:th {:scope :col} "#"]
       [:th {:scope :col} "Title"]
       [:th {:scope :col} "Author"]]]
     [:tbody
      (map render-post posts)]]]))
