(ns user
  (:require
   [com.stuartsierra.component :as component]
   [system]))

(def system (system/build))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system component/stop))
