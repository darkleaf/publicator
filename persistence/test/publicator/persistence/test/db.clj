(ns publicator.persistence.test.db
  (:require
   [publicator.persistence.components.data-source :as data-source]
   [publicator.persistence.components.migration :as migration]
   [com.stuartsierra.component :as component]
   [jdbc.core :as jdbc]))

(defn- build-system [database]
  (component/system-map
   :data-source (data-source/build {:jdbc-url (str "jdbc:postgresql://db/" database)
                                    :user "postgres"
                                    :password "password"})
   :migration (component/using (migration/build)
                               [:data-source])))

(defn- with-system [database f]
  (let [system (build-system database)
        system (component/start system)]
    (try
      (f system)
      (finally
        (component/stop system)))))

;; Жирно на каждый тест держать коннект к базе.
;; Хорошо бы иметь глобальный тредпул,
;; но тогда нужно заморачиваться с правильным code reload.
(let [counter (atom 0)]
  (defn- with-test-database [f]
    (let [spec     "postgresql://postgres:password@db/postgres"
          [num _]  (swap-vals! counter inc)
          database (str "test_" num)]
      (with-open [conn (jdbc/connection spec)]
        (jdbc/execute conn (str "create database " database))
        (try
          (f database)
          (finally
            (jdbc/execute conn (str "drop database " database))))))))

(declare ^:dynamic *data-source*)

(defn fixture [t]
  (with-test-database
    (fn [database]
      (with-system database
        (fn [system]
          (binding [*data-source* (-> system :data-source :val)]
            (t)))))))
