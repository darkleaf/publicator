(ns publicator.web.handler
  (:require
   [ring.middleware.params :as ring.params]
   [ring.middleware.keyword-params :as ring.keyword-params]
   [ring.middleware.anti-forgery :as ring.anti-forgery]
   [publicator.web.routing :as routing]
   [publicator.web.responders.base :as responders.base]
   [publicator.web.middlewares.layout :as layout]
   [publicator.web.middlewares.session :as session]
   [publicator.web.middlewares.transit-params :as tranist-params]
   [publicator.web.transit :as t]


   [publicator.web.responders.pages.root]
   [publicator.web.responders.user.log-in]
   [publicator.web.responders.user.log-out]
   [publicator.web.responders.user.register]
   [publicator.web.responders.post.list]
   [publicator.web.responders.post.show]
   [publicator.web.responders.post.create]
   [publicator.web.responders.post.update]))


(defn- wrap-reponders [handler]
  (fn [req]
    (let [[interactor & args] (handler req)
          result              (apply interactor args)]
      (responders.base/->resp result args))))

(defn build
  ([] (build {}))
  ([config]
   (let [handler (sibiro.extras/make-handler routing/routes)]
     (-> routing/handler
         wrap-reponders
         layout/wrap
         ring.anti-forgery/wrap-anti-forgery
         (session/wrap (:session config {}))
         tranist-params/wrap-transit-params
         ring.keyword-params/wrap-keyword-params
         ring.params/wrap-params))))
