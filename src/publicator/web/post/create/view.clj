(ns publicator.web.post.create.view
  (:require
   [hiccup.core :refer [html]]
   [io.pedestal.http.route :as route]
   [form-ujs.core :as form]
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
  (let [desc (form/spec->widget spec)]
    {:id :create
     :widget :submit
     :url (route/url-for :post-create)
     :method :post
     :body desc}))

(defn render [spec params errors]
  (form (description spec)
        params
        errors))
