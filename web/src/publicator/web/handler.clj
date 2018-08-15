(ns publicator.web.handler
  (:require
   [ring.middleware.params :as ring.params]
   [ring.middleware.keyword-params :as ring.keyword-params]
   [ring.middleware.anti-forgery :as ring.anti-forgery]
   [ring.util.request :as ring.request]
   [publicator.web.routing :as routing]
   [publicator.web.responders.base :as responders.base]
   [publicator.web.middlewares.layout :as layout]
   [publicator.web.middlewares.session :as session]
   [publicator.web.transit :as t]


   [publicator.web.responders.pages.root]
   [publicator.web.responders.user.log-in]
   [publicator.web.responders.user.log-out]
   [publicator.web.responders.user.register]
   [publicator.web.responders.post.list]
   [publicator.web.responders.post.show]
   [publicator.web.responders.post.create]
   [publicator.web.responders.post.update]))


(defn- wrap-transit-params [handler]
  (fn [req]
    (let [req (if (= "application/transit+json"
                     (ring.request/content-type req))
                (assoc req :transit-params (-> req
                                               ring.request/body-string
                                               t/read))
                req)]
      (handler req))))

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
         wrap-transit-params
         ring.keyword-params/wrap-keyword-params
         ring.params/wrap-params))))
