(ns publicator.domain.abstractions.instant-testing
  (:require
   [publicator.domain.abstractions.instant :as sut]
   [clojure.test :as t]))

(defn testing []
  (t/is (inst? (sut/now))))
