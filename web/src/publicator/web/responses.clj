(ns publicator.web.responses
  (:require
   [publicator.web.template :as template]
   [publicator.web.form-renderer :as form-renderer]
   [publicator.web.transit :as transit]
   [ring.util.http-response :as http-response]))

(defn render-page
  ([template] (render-page template {}))
  ([template model]
   (-> (template/render template model)
       (http-response/ok)
       (http-response/content-type "text/html"))))

(defn render-form [form]
  (-> form
      form-renderer/render
      http-response/ok
      (http-response/content-type "text/html")))

(defn render-errors [errors]
  (-> errors
      transit/write
      http-response/unprocessable-entity
      (http-response/content-type "application/transit+json")))

(defn redirect-for-page [url]
  (http-response/found url))

(defn redirect-for-form [url]
  (http-response/created url))
