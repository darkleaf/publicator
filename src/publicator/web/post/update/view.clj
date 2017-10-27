(ns publicator.web.post.update.view
  (:require
   [hiccup.core :refer [html]]
   [io.pedestal.http.route :as route]
   [form-ujs.core :as form]
   [publicator.domain.post :as post]
   [publicator.web
    [transit :as t]]))

;; move to form-ujs
(defn form [description data errors]
  (html
   [:div
    [:div {:data-form-ujs :register}]
    [:script
     {:id "register-description"
      :type "application/transit+json"}
     (t/write description)]
    [:script
     {:id "register-data"
      :type "application/transit+json"}
     (t/write data)]]))

(defn description [spec]
  (let [desc (form/spec->widget spec)
        desc (assoc-in desc [:items ::post/content :widget] :textarea)]
    {:id     :create
     :widget :submit
     :url    (route/url-for :post-update)
     :method :patch
     :body   desc}))

(defn render [spec params errors]
  (form (description spec)
        params
        errors))
