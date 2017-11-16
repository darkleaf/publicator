(ns publicator.impl.hasher-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.test :as t]
   [publicator.domain.abstractions.hasher :as hasher]
   [publicator.impl.hasher :as impl.hasher]))

(t/use-fixtures :each
  (fn [t]
    (with-bindings (impl.hasher/binding-map)
      (t))))

(t/deftest work
  (let [password (-> (s/and string?
                            #(< (count %) 255))
                     (s/gen)
                     (sgen/generate))
        digest (hasher/derive password)]
    (t/is (hasher/check password digest))))
