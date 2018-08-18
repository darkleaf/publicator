(ns publicator.main.handler
  (:require
   [com.stuartsierra.component :as component]
   [publicator.web.handler :as handler]))

(defrecord Handler [binding-map val]
  component/Lifecycle
  (start [this]
    (assoc this :val
           (fn [req]
             (with-bindings (:val binding-map)
               (handler/handler req)))))
  (stop [this] this))

(defn build []
  (->Handler nil nil))
