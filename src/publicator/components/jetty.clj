(ns publicator.components.jetty
  (:require
   [com.stuartsierra.component :as component]
   [ring.adapter.jetty :as jetty]
   [publicator.impl.ring-session :as ring-session]
   [publicator.ring.handler :as handler]))

(defn- wrap-binding [handler binding-map]
  (fn [req]
    (with-bindings binding-map
      (handler req))))

(defrecord Jetty [config implementations server]
  component/Lifecycle
  (start [this]
    (if server
      this
      (assoc this :server
             (jetty/run-jetty
              (-> (handler/build)
                  (wrap-binding (:binding-map implementations))
                  (ring-session/wrap-session))
              (assoc config :join? false)))))
  (stop [this]
    (if server
      (do
        (.stop server)
        (assoc this :server nil))
      this)))

(defn build [config]
  (Jetty. config nil nil))
