(ns publicator.domain.identity-test
  (:require
   [publicator.utils.test.instrument]
   [publicator.domain.identity :as sut]
   [publicator.domain.abstractions.aggregate :as aggregate]
   [clojure.spec.alpha :as s]
   [clojure.test :as t]))

(defrecord Aggregate [id property]
  aggregate/Aggregate
  (id [_] id)
  (spec [_] (fn [_] (some? property))))

(defrecord OtherAggregate [id property]
  aggregate/Aggregate
  (id [_] id)
  (spec [_] (fn [_] (some? property))))

(t/deftest identity-test
  (let [iagg (sut/build (->Aggregate 1 true))]
    (t/testing "validator"
      (t/is (thrown-with-msg? clojure.lang.ExceptionInfo
                              #"Aggregate id was changed."
                              (dosync (alter iagg assoc :id 2))))
      (t/is (thrown-with-msg? clojure.lang.ExceptionInfo
                              #"Aggregate class was changed."
                              (dosync (alter iagg map->OtherAggregate))))
      (t/is (thrown-with-msg? clojure.lang.ExceptionInfo
                              #"Aggregate was invalid. "
                              (dosync (alter iagg assoc :property nil)))))))
