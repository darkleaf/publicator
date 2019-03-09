(ns publicator.domain.abstractions.scaffolding
  (:require
   [publicator.domain.abstractions.id-generator-fake :as id-generator-fake]
   [publicator.domain.abstractions.instant-impl :as instant-impl]
   [publicator.domain.abstractions.password-hasher-fake :as password-hasher-fake]))

(defn setup [body]
  (let [binding-map (merge (id-generator-fake/binding-map)
                           (instant-impl/binding-map)
                           (password-hasher-fake/binding-map))]
    (with-bindings binding-map
      (body))))
