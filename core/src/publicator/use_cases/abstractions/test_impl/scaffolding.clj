(ns publicator.use-cases.abstractions.test-impl.scaffolding
  (:require
   [publicator.domain.abstractions.test-impl.scaffolding :as domain-scaffolding]
   [publicator.use-cases.abstractions.test-impl.session-fake :as session-fake]
   [publicator.use-cases.abstractions.test-impl.storage-fake  :as storage-fake]))

(defn setup [body]
  (let [db          (storage-fake/build-db)
        binding-map (merge (session-fake/binding-map)
                           (storage-fake/binding-map db))]
    (with-bindings binding-map
      (domain-scaffolding/setup body))))
