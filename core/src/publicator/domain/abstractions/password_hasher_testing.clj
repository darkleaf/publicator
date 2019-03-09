(ns publicator.domain.abstractions.password-hasher-testing
  (:require
   [publicator.domain.abstractions.password-hasher :as password-hasher]
   [clojure.test :as t]))

(def ^:const ^:private pass "pass")

(defn test-derive []
  (t/is (not= pass (password-hasher/*derive* pass))))

(defn test-derive-&-check []
  (let [digest (password-hasher/*derive* pass)]
    (t/is (password-hasher/*check* pass digest))
    (t/is (not (password-hasher/*check* "wrong" digest)))))
