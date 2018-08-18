(ns publicator.web.components.handler
  (:require
   [com.stuartsierra.component :as component]
   [publicator.web.handler :as handler]))

(defrecord Handler [binding-map val]
  component/Lifecycle
  (start [this]
    (assoc this :val (handler/build (:val binding-map))))
  (stop [this] this))

(defn build []
  (->Handler nil nil))
