(ns publicator.web.post.update.view
  (:require
   [hiccup.core :refer [html]]
   [publicator.ring.helpers :refer [path-for]]
   [form-ujs.core :as form]
   [form-ujs.html :refer [form]]
   [publicator.domain.post :as post]))

(defn description [post-id spec]
  (let [desc (form/spec->widget spec)
        desc (assoc-in desc [:items ::post/content :widget] :textarea)]
    {:id     :create
     :widget :submit
     :url    (path-for :post.update/handler {:id post-id})
     :method :patch
     :body   desc}))

(defn render [post-id spec params errors]
  (form (description post-id spec)
        params
        errors))
