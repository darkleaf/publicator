(ns publicator.web.handler
  (:require
   [sibiro.extras]
   [ring.middleware.params :as ring.params]
   [ring.middleware.keyword-params :as ring.keyword-params]
   [ring.middleware.anti-forgery :as ring.anti-forgery]
   [ring.util.request :as ring.request]
   [publicator.web.routing :as routing]
   [publicator.web.middlewares.layout :as layout]
   [publicator.web.middlewares.session :as session]
   [publicator.web.transit :as t]))

(defn- wrap-transit-params [handler]
  (fn [req]
    (let [req (if (= "application/transit+json"
                     (ring.request/content-type req))
                (assoc req :transit-params (-> req
                                               ring.request/body-string
                                               t/read))
                req)]
      (handler req))))

(defn build
  ([] (build {}))
  ([config]
   (let [handler (sibiro.extras/make-handler routing/routes)
         test?   (:test? config)]
     (cond-> handler
       true        layout/wrap
       (not test?) ring.anti-forgery/wrap-anti-forgery
       true        (session/wrap (:session config {}))
       true        wrap-transit-params
       true        ring.keyword-params/wrap-keyword-params
       true        ring.params/wrap-params))))
