(ns publicator.ring.handler
  (:require
   [sibiro.extras]
   [ring.middleware.session :as ring.session]
   [ring.middleware.params :as ring.params]
   [ring.middleware.keyword-params :as ring.keyword-params]
   [publicator.ring.routes :as routes]
   [publicator.ring.helpers :as helpers]
   [publicator.interactors.abstractions.session :as abstractions.session]
   [publicator.web.layout :as layout]
   [publicator.transit :as transit]))

(deftype Session [storage]
  abstractions.session/Session
  (-get [_ k] (get @storage k))
  (-set! [_ k v] (swap! storage assoc k v)))


;; todo: move to cookie
(defn- wrap-session [handler]
  (ring.session/wrap-session
   (fn [req]
     (let [storage (atom (:session req))
           session (Session. storage)
           resp    (binding [abstractions.session/*session* session]
                     (handler req))
           resp    (assoc resp :session/key (:session/key req))
           resp    (assoc resp :session @storage)]
       resp))))

(defn- wrap-binding [handler binding-map]
  (fn [req]
    (with-bindings binding-map
      (handler req))))

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

(defn build [binding-map]
  (let [routes (routes/build)]
    (->  (sibiro.extras/make-handler routes)
         (wrap-layout)
         (wrap-binding binding-map)
         (wrap-session)
         (wrap-routes routes)
         (wrap-transit)
         (wrap-method-override)
         (ring.keyword-params/wrap-keyword-params)
         (ring.params/wrap-params))))
