(ns publicator.domain.abstractions.testing.occupation
  (:require
   [publicator.domain.abstractions.occupation :as occupation]
   [clojure.test :as t]))

(defn test-return-value-&-args []
  (t/is (boolean? (occupation/*occupied* #{:test/a :test/b}
                                         {:test/a 1, :test/b [1 2]})))
  (t/is (boolean? (occupation/*occupied* #{:test/a :test/b}
                                         {:test/a 1})))
  (t/is (boolean? (occupation/*occupied* #{:test/a :test/b}
                                         {:test/b [1 2]}))))
