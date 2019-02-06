(ns publicator-ext.domain.abstractions.scaffolding
  (:require
   [publicator-ext.domain.abstractions.id-generator-fake :as id-generator]
   [publicator-ext.domain.abstractions.instant-impl :as instant]))

(defn setup [body]
  (let [binding-map (merge (id-generator/binding-map)
                           (instant/binding-map))]
    (with-bindings binding-map
      (body))))
