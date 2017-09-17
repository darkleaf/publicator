(ns user
  (:require
   [publicator.system :as system]
   [com.stuartsierra.component :as component]
   [clojure.spec.alpha :as s]))

(s/check-asserts true)

(def system (system/build))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system component/stop))

(start)

#_(stop)
