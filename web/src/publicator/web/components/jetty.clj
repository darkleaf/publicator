(ns publicator.web.components.jetty
  (:require
   [com.stuartsierra.component :as component]
   [ring.adapter.jetty :as jetty]))

(defrecord Jetty [config handler val]
  component/Lifecycle
  (start [this]
    (if val
      this
      (assoc this :val
             (jetty/run-jetty
              (:val handler)
              (assoc config :join? false)))))
  (stop [this]
    (if val
      (do
        (.stop val)
        (assoc this :val nil))
      this)))

(defn build [config]
  (Jetty. config nil nil))
