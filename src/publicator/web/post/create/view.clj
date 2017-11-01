(ns publicator.web.post.create.view
  (:require
   [hiccup.core :refer [html]]
   [publicator.ring.helpers :refer [path-for]]
   [form-ujs.spec.widget :refer [spec->widget]]
   [form-ujs.html :refer [form]]
   [publicator.domain.post :as post]))

(defn description [spec]
  (let [desc (spec->widget spec)
        desc (assoc-in desc [:items ::post/content :widget] :textarea)]
    {:id     :create
     :widget :submit
     :url    (path-for :post.create/handler)
     :method :post
     :body   desc}))

(defn render-form [spec params]
  (form (description spec)
        params))
