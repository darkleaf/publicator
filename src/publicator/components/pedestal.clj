(ns publicator.components.pedestal
  (:require
   [com.stuartsierra.component :as component]
   [io.pedestal.http :as http]
   [publicator.pedestal.service :as service]))

(defrecord Pedestal [interactor-ctx server]
  component/Lifecycle
  (start [this]
    (if server
      this
      (assoc this :server
             (-> (service/build (:ctx interactor-ctx))
                 (http/create-server)
                 (http/start)))))
  (stop [this]
    (if server
      (do
        (http/stop server)
        (assoc this :server nil))
      this)))

(defn build []
  (Pedestal. nil nil))
