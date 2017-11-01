(ns publicator.ring.handler
  (:require
   [sibiro.extras]
   [ring.middleware.params :as ring.params]
   [ring.middleware.keyword-params :as ring.keyword-params]
   [publicator.ring.routes :as routes]
   [publicator.ring.helpers :as helpers]
   [publicator.web.layout :as layout]
   [publicator.transit :as transit]))

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

(defn- wrap-transit [handler]
  (fn [req]
    (handler
     (if (= "application/transit+json"
            (:content-type req))
       (let [body   (:body req)
             params (transit/read-stream body)]
         (assoc req :transit-params params))
       req))))

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
         (wrap-transit)
         (wrap-method-override)
         (ring.keyword-params/wrap-keyword-params)
         (ring.params/wrap-params))))
