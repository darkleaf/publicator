(ns publicator.components.jetty
  (:require
   [com.stuartsierra.component :as component]
   [ring.adapter.jetty :as jetty]
   [publicator.ring.handler :as handler]))

(defrecord Jetty [implementations server]
  component/Lifecycle
  (start [this]
    (if server
      this
      (assoc this :server
             (jetty/run-jetty (handler/build (:binding-map implementations))
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
