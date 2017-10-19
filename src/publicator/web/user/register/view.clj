(ns publicator.web.user.register.view
  (:require
   [hiccup.core :refer [html]]
   [form-ujs.core :as form]
   [publicator.interactors.user.register :as register]
   [cognitect.transit :as t])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]))

(defn serialize [d]
  (let [out (ByteArrayOutputStream. 4096)
        writer (t/writer out :json)
        _ (t/write writer d)]
    (.toString out)))

(defn form [description data errors]
  (html
   [:div
    [:div {:data-form-ujs :register}]
    [:script
     {:id "register-description"
      :type "application/transit+json"}
     (serialize description)]
    [:script
     {:id "register-data"
      :type "application/transit+json"}
     (serialize data)]]))

(defn description []
  (let [desc (form/spec->widget ::register/params)]
    {:id :register
     :widget :submit
     :url "/registration"
     :method :post
     :body desc}))

(defn page [ctx]
  (form (description)
        {} ;; initial params
        nil))

(defn render [ctx]
  (page ctx))
