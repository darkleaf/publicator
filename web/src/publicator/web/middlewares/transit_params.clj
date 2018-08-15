(ns publicator.web.middlewares.transit-params
  (:require
   [publicator.web.transit :as transit]
   [ring.util.request :as ring.request]))

(defn wrap-transit-params [handler]
  (fn [req]
    (handler
     (cond-> req
       (= "application/transit+json" (ring.request/content-type req))
       (assoc  :transit-params (-> req ring.request/body-string transit/read))))))
