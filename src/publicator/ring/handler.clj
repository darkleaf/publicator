(ns publicator.ring.handler
  (:require
   [sibiro.extras]
   [ring.middleware.params :as ring.params]
   [ring.middleware.keyword-params :as ring.keyword-params]
   [ring.middleware.resource :as ring.resource]
   [publicator.ring.routes :as routes]
   [publicator.ring.helpers :as helpers]
   [publicator.web.layout :as layout]))

(defn- wrap-routes [handler routes]
  (fn [req]
    (binding [helpers/*routes* routes]
      (handler req))))

(defn- wrap-layout [handler]
  (fn [req]
    (let [resp (handler req)]
      (if (= (get-in resp [:headers "Content-Type"])
             "text/html")
        (update resp :body layout/render)
        resp))))

(defn- wrap-method-override [handler]
  (fn [req]
    (let [method (get-in req [:params :_method] (:request-method req))
          method (keyword method)
          req    (assoc req :request-method method)]
      (handler req))))

(defn build []
  (let [routes (routes/build)]
    (->  (sibiro.extras/make-handler routes)
         (wrap-layout)
         (wrap-routes routes)
         (wrap-method-override)
         (ring.keyword-params/wrap-keyword-params)
         (ring.params/wrap-params)

         ;; todo: fix security
         (ring.resource/wrap-resource "form_ujs"))))
