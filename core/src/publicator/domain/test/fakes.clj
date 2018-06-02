(ns publicator.domain.test.fakes
  (:require
   [publicator.domain.test.fakes.password-hasher :as password-hasher]
   [publicator.domain.test.fakes.id-generator :as id-generator]))

(defn fixture [f]
  (let [binding-map (merge (password-hasher/binding-map)
                           (id-generator/binding-map))]
    (with-bindings binding-map
      (f))))
