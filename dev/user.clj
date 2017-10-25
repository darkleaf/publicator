(ns user
  (:require
   [publicator.system :as system]
   [publicator.factories :as factrories]
   [com.stuartsierra.component :as component]
   [clojure.spec.alpha :as s]))

(s/check-asserts true)

(def system (system/build))

(defn- seed []
  (with-bindings (get-in system [:implementations :binding-map])
    (factrories/create-user :login "admin"
                            :password "12345678")
    (factrories/create-post)
    (factrories/create-post)))

(defn start []
  (alter-var-root #'system component/start)
  (seed))

(defn stop []
  (alter-var-root #'system component/stop))

#_(start)

#_(stop)
