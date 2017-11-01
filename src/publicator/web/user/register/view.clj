(ns publicator.web.user.register.view
  (:require
   [hiccup.core :refer [html]]
   [publicator.ring.helpers :refer [path-for]]
   [form-ujs.spec.widget :refer [spec->widget]]
   [form-ujs.html :refer [form]]))

(defn description [spec]
  (let [desc (spec->widget spec)]
    {:id :register
     :widget :submit
     :url (path-for :user.register/handler)
     :method :post
     :body desc}))

(defn render [spec params errors]
  (form (description spec)
        params
        errors))
