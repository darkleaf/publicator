(ns publicator.web.user.log-in.view
  (:require
   [hiccup.core :refer [html]]
   [io.pedestal.http.route :as route]
   [form-ujs.core :as form]
   [form-ujs.html :refer [form]]))

(defn description [spec]
  (let [desc (form/spec->widget spec)]
    {:id :register
     :widget :submit
     :url (route/url-for :user.log-in/handler)
     :method :post
     :body desc}))

(defn render [spec params errors]
  (form (description spec)
        params
        errors))
