(ns publicator.use-cases.abstractions.testing.session
  (:require
   [publicator.use-cases.abstractions.session :as session]
   [clojure.test :as t]))

(defn test-get-&-set []
  (session/*set* :key :value)
  (t/is (= :value (session/*get* :key))))
