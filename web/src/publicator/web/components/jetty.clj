(ns publicator.web.components.jetty
  (:require
   [com.stuartsierra.component :as component]
   [ring.adapter.jetty :as jetty]
   [publicator.web.handler :as handler]))

(defn- wrap-binding [handler binding-map]
  (fn [req]
    (with-bindings binding-map
      (handler req))))

(defrecord Jetty [config binding-map val]
  component/Lifecycle
  (start [this]
    (if val
      this
      (assoc this :val
             (jetty/run-jetty
              (-> (handler/build config)
                  (wrap-binding (:val binding-map)))
              (assoc config :join? false)))))
  (stop [this]
    (if val
      (do
        (.stop val)
        (assoc this :val nil))
      this)))

(defn build [config]
  (Jetty. config nil nil))
