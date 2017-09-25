(ns user
  (:require
   [publicator.system :as system]
   [com.stuartsierra.component :as component]
   [clojure.spec.alpha :as s]))

(s/check-asserts true)

(def system nil)

(defn start []
  (alter-var-root #'system
                  (constantly
                   (-> (system/build)
                       (component/start)))))

(defn stop []
  (alter-var-root #'system component/stop))

#_(start)

#_(stop)
