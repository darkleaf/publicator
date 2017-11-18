(ns publicator.web.user.register.view
  (:require
   [hiccup.core :refer [html]]
   [publicator.ring.helpers :refer [path-for]]
   [form-ujs.spec.widget :refer [spec->widget]]
   [form-ujs.html :refer [form]]
   [form-ujs.errors :as errors]))

(defn description [spec]
  (let [desc (spec->widget spec)]
    {:id :register
     :widget :submit
     :url (path-for :user.register/handler)
     :method :post
     :body desc}))

(defn render-form [spec params]
  (form (description spec)
        params))

(defn already-registered []
  (errors/add-message (errors/blank) [] "Уже зарегистрирован"))
