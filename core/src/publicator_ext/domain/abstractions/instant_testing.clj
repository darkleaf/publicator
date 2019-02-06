(ns publicator-ext.domain.abstractions.instant-testing
  (:require
   [publicator-ext.domain.abstractions.instant :as sut]
   [clojure.test :as t]))

(defn testing []
  (t/is (inst? (sut/now))))
