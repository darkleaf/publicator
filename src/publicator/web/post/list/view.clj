(ns publicator.web.post.list.view
  (:require
   [hiccup.core :refer [html]]
   [io.pedestal.http.route :as route]
   [publicator.interactors.services.user-session :as user-session]
   [publicator.web.helpers :as h]))

(defn- can-operate? [item]
  (= (user-session/user-id)
     (:author-id item)))

(defn- render-post [{:keys [id title author-full-name] :as item}]
  (html
   [:tr
    [:th {:scope :row} id]
    [:td (h/link-to title (route/url-for :post-show :path-params {:id id}))]
    [:td author-full-name]
    [:td
     (when (can-operate? item)
       [:div
        (h/link-to "Edit" (route/url-for :post-update-form :path-params {:id 2}))])]]))

(defn render [items]
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
       [:th {:scope :col} "Author"]
       [:th {:scope :col} "Actions"]]]
     [:tbody
      (map render-post items)]]]))
