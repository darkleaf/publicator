(ns publicator.web.user.log-in.view
  (:require
   [hiccup.core :refer [html]]
   [publicator.ring.helpers :refer [path-for]]
   [form-ujs.spec.widget :refer [spec->widget]]
   [form-ujs.html :refer [form]]
   [form-ujs.errors :as errors]))

(defn- description [spec]
  (let [desc (spec->widget spec)]
    {:id :register
     :widget :submit
     :url (path-for :user.log-in/handler)
     :method :post
     :body desc}))

(defn render-from [spec params]
  (form (description spec)
        params))

(defn authentication-failed []
  (errors/add-message (errors/blank) [] "Неправильный логин или пароль"))
