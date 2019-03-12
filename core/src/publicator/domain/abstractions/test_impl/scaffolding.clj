(ns publicator.domain.abstractions.test-impl.scaffolding
  (:require
   [publicator.domain.abstractions.test-impl.id-generator-fake :as id-generator-fake]
   [publicator.domain.abstractions.test-impl.password-hasher-fake :as password-hasher-fake]
   [publicator.domain.abstractions.test-impl.occupation-stub :as occupation-stub]))

(defn setup [body]
  (let [binding-map (merge (id-generator-fake/binding-map)
                           (password-hasher-fake/binding-map)
                           (occupation-stub/binding-map))]
    (with-bindings binding-map
      (body))))
