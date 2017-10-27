(ns publicator.web.post.update.view
  (:require
   [hiccup.core :refer [html]]
   [io.pedestal.http.route :as route]
   [form-ujs.core :as form]
   [form-ujs.html :refer [form]]
   [publicator.domain.post :as post]))

(defn description [spec]
  (let [desc (form/spec->widget spec)
        desc (assoc-in desc [:items ::post/content :widget] :textarea)]
    {:id     :create
     :widget :submit
     :url    (route/url-for :post.update/handler)
     :method :patch
     :body   desc}))

(defn render [spec params errors]
  (form (description spec)
        params
        errors))
