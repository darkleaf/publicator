(ns publicator.web.post.list.view
  (:require
   [hiccup.core :refer [html]]
   [publicator.interactors.services.user-session :as user-session]
   [publicator.ring.helpers :refer [path-for]]
   [publicator.web.helpers :as h]
   [publicator.domain.post :as post]))

;; динамическая типизация позволяет это делать.
;; item - неполное представление post
;; для проверки author? данных в item достаточно
;; может быть есть более правильный вариант
(defn- can-operate? [item]
  (let [fake-post item
        user (user-session/user)]
    (post/author? fake-post user)))

(defn- render-post [{:keys [id title author-full-name] :as item}]
  (html
   [:tr
    [:th {:scope :row} id]
    [:td (h/link-to title (path-for :post.show/handler {:id id}))]
    [:td author-full-name]
    [:td
     (when (can-operate? item)
       [:div
        (h/link-to "Edit" (path-for :post.update/form {:id id})
                   :class "btn btn-sm btn-primary mr-3")
        (h/action-btn "Destroy"
                      (path-for :post.destroy/handler
                                {:id id
                                 :_method "delete"})
                      :class "btn btn-sm btn-warning")])]]))

(defn render [items]
  (html
   [:div
    (if (user-session/logged-in?)
      (h/link-to "New post" (path-for :post.create/form)
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
