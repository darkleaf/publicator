(ns publicator.web.handler
  (:require
   [ring.middleware.session :as ring.session]
   [ring.middleware.params :as ring.params]
   [ring.middleware.keyword-params :as ring.keyword-params]
   [ring.middleware.anti-forgery :as ring.anti-forgery]
   [publicator.web.routing :as routing]
   [publicator.web.middlewares.layout :as layout]
   [publicator.web.middlewares.session :as session]
   [publicator.web.middlewares.transit-params :as tranist-params]
   [publicator.web.middlewares.responder :as responder]))

(def handler
  (-> routing/handler

      responder/wrap-reponder
      layout/wrap-layout
      session/wrap-session
      tranist-params/wrap-transit-params

      ring.anti-forgery/wrap-anti-forgery
      ring.session/wrap-session
      ring.keyword-params/wrap-keyword-params
      ring.params/wrap-params))
