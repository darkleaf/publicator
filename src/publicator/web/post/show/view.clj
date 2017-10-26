(ns publicator.web.post.show.view
  (:require
   [hiccup.core :refer [html]]
   [io.pedestal.http.route :as route]
   [publicator.interactors.abstractions.storage :as storage]))

(defn render [post]
  (html
   [:div.mt-3
    [:h1 (:title post)]
    [:div.text-muted
     "Author: "
     (-> post
         :author-id
         (storage/tx-get-one)
         :full-name)]
    (:content post)]))
