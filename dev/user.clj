(ns user
  (:require
   [publicator.init]
   [com.stuartsierra.component :as component]
   [publicator.systems.fake :as systems.fake]
   [publicator.systems.impl :as systems.impl]
   [clojure.spec.alpha :as s]))

(s/check-asserts true)

(def fake-system (systems.fake/build
                  {:http-opts {:host "0.0.0.0", :port 4101}}))
(def impl-system (systems.impl/build
                  {:http-opts {:host "0.0.0.0", :port 4102}
                   :data-source-opts {:jdbc-url "jdbc:postgresql://db/development"
                                      :user "postgres"
                                      :password "password"}}))

(defn start []
  (alter-var-root #'fake-system component/start)
  (alter-var-root #'impl-system component/start))

(defn stop []
  (alter-var-root #'fake-system component/stop)
  (alter-var-root #'impl-system component/stop))

(comment
  (start)
  (stop))
