(ns publicator.use-cases.abstractions.password-hasher-testing
  (:require
   [publicator.use-cases.abstractions.password-hasher :as sut]
   [clojure.test :as t]))

(def ^:const ^:private pass "pass")

(defn test-derive []
  (t/is (not= pass (sut/*derive* pass))))

(defn test-derive-&-check []
  (let [digest (sut/*derive* pass)]
    (t/is (sut/*check* pass digest))
    (t/is (not (sut/*check* "wrong" digest)))))
