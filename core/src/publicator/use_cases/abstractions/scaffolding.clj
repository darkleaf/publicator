(ns publicator.use-cases.abstractions.scaffolding
  (:require
   [publicator.domain.abstractions.scaffolding :as domain-scaffolding]
   [publicator.use-cases.abstractions.session-fake :as session]
   [publicator.use-cases.abstractions.storage-fake  :as storage]))

(defn setup [body]
  (let [db          (storage/build-db)
        binding-map (merge (session/binding-map)
                           (storage/binding-map db))]
    (with-bindings binding-map
      (domain-scaffolding/setup body))))
