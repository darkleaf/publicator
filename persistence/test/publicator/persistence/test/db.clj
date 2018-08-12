(ns publicator.persistence.test.db
  (:require
   [publicator.persistence.components.data-source :as data-source]
   [publicator.persistence.components.migration :as migration]
   [publicator.persistence.utils.env :as env]
   [com.stuartsierra.component :as component]
   [jdbc.core :as jdbc]
   [hugsql.core :as hugsql]
   [hugsql.adapter.clojure-jdbc :as cj-adapter]))

(hugsql/def-db-fns "publicator/persistence/test/db.sql"
  {:adapter (cj-adapter/hugsql-adapter-clojure-jdbc)
   :quoting :ansi})

(defn- build-system []
  (component/system-map
   :data-source (data-source/build (env/data-source-opts "TEST_DATABASE_URL"))
   :migration (component/using (migration/build)
                               [:data-source])))

(defn- with-system [f]
  (let [system (atom (build-system))]
    (try
      (swap! system component/start)
      (f @system)
      (finally
        (swap! system component/stop)))))

(declare ^:dynamic *data-source*)

(defn once-fixture [t]
  (with-system
    (fn [system]
      (let [data-source (-> system :data-source :val)]
        (binding [*data-source* data-source]
          (t))))))

(defn each-fixture [t]
  (try
    (t)
    (finally
      (with-open [conn (jdbc/connection *data-source*)]
        (truncate-all conn)))))
