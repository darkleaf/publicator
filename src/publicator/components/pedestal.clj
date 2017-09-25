(ns publicator.components.pedestal
  (:require
   [publicator.controllers.registration :as registration]
   [com.stuartsierra.component :as component]
   [io.pedestal.http :as http]
   [io.pedestal.http.route :as route]))

(defn routes []
  (route/expand-routes
   (-> #{}
       (into (registration/routes)))))

(defn- build-server []
  (http/create-server
   {::http/routes (routes)
    ::http/join?  false
    ::http/type   :jetty
    ::http/port   4101
    ::http/secure-headers {:content-security-policy-settings {:object-src "none"}}}))

(defrecord Pedestal [server]
  component/Lifecycle
  (start [this]
    (if server
      this
      (let [server (build-server)]
        (http/start server)
        (assoc this :server server))))
  (stop [this]
    (if server
      (do
        (http/stop server)
        (assoc this :server nil))
      this)))


(defn build [] (->Pedestal nil))
