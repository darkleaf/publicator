(ns publicator.web.post.create.view
  (:require
   [hiccup.core :refer [html]]
   [publicator.ring.helpers :refer [path-for]]
   [form-ujs.core :as form]
   [form-ujs.html :refer [form]]
   [publicator.domain.post :as post]))

(defn description [spec]
  (let [desc (form/spec->widget spec)
        desc (assoc-in desc [:items ::post/content :widget] :textarea)]
    {:id     :create
     :widget :submit
     :url    (path-for :post.create/handler)
     :method :post
     :body   desc}))

(defn render [spec params errors]
  (form (description spec)
        params
        errors))
