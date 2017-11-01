(ns publicator.components.jetty
  (:require
   [com.stuartsierra.component :as component]
   [ring.adapter.jetty :as jetty]
   [publicator.impl.session :as session]
   [publicator.ring.handler :as handler]))

(defn- wrap-binding [handler binding-map]
  (fn [req]
    (with-bindings binding-map
      (handler req))))

(defrecord Jetty [implementations server]
  component/Lifecycle
  (start [this]
    (if server
      this
      (assoc this :server
             (jetty/run-jetty
              (-> (handler/build)
                  (wrap-binding (:binding-map implementations))
                  (session/wrap-session))
              {:host  "0.0.0.0"
               :port  4101
               :join? false}))))
  (stop [this]
    (if server
      (do
        (.stop server)
        (assoc this :server nil))
      this)))

(defn build []
  (Jetty. nil nil))
