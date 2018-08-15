(ns publicator.web.handler
  (:require
   [ring.middleware.params :as ring.params]
   [ring.middleware.keyword-params :as ring.keyword-params]
   [ring.middleware.anti-forgery :as ring.anti-forgery]
   [publicator.web.routing :as routing]
   [publicator.web.middlewares.layout :as layout]
   [publicator.web.middlewares.session :as session]
   [publicator.web.middlewares.transit-params :as tranist-params]
   [publicator.web.middlewares.responder :as responder]))

(defn build
  ([] (build {}))
  ([config]
   (let [handler (sibiro.extras/make-handler routing/routes)]
     (-> routing/handler
         responder/wrap-reponder
         layout/wrap-layout
         ring.anti-forgery/wrap-anti-forgery
         (session/wrap-session (:session config {}))
         tranist-params/wrap-transit-params
         ring.keyword-params/wrap-keyword-params
         ring.params/wrap-params))))
