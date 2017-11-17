(ns user
  (:require
   [publicator.init]
   [publicator.impl.test-db :as test-db]
   [publicator.db.migration :as migration]
   [com.stuartsierra.component :as component]
   [dev.fake-system :as fake-system]
   [dev.impl-system :as impl-system]
   [clojure.spec.alpha :as s]))

(s/check-asserts true)

(def fake-system (fake-system/build))
(def impl-system (impl-system/build))

(defn start []
  (alter-var-root #'fake-system component/start)
  (alter-var-root #'impl-system component/start))

(defn stop []
  (alter-var-root #'fake-system component/stop)
  (alter-var-root #'impl-system component/stop))

(defn migrate []
  (migration/migrate (get-in impl-system [:data-source :data-source]))
  (migration/migrate test-db/data-source))

(comment
  (start)
  (stop)
  (migrate))
