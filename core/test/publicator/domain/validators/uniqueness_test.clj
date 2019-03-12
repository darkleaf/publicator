(ns publicator.domain.validators.uniqueness-test
  (:require
   [publicator.domain.validators.uniqueness :as uniqueness]
   [publicator.domain.abstractions.occupation :as occupation]
   [publicator.utils.datascript.validation :as d.validation]
   [datascript.core :as d]
   [clojure.test :as t]))

(t/deftest validator
  (let [validator (uniqueness/validator #{:attr})]
    (t/testing "changed"
      (let [report    (-> (d/empty-db)
                          (d/with [[:db/add 1 :attr :val]]))]
        (t/testing "not occupied"
          (binding [occupation/*occupied* (constantly false)]
            (let [errors (d.validation/validate report validator)]
              (t/is (empty? errors)))))
        (t/testing "occupied"
          (binding [occupation/*occupied* (constantly true)]
            (let [errors (d.validation/validate report validator)]
              (t/is (not-empty errors)))))))
    (t/testing "not changed"
      (let [report    (-> (d/empty-db)
                          (d/db-with [[:db/add 1 :attr :val]])
                          (d/with    [[:db/add 1 :attr :val]]))]
        (t/testing "occupied"
          (binding [occupation/*occupied* (constantly true)]
            (let [errors (d.validation/validate report validator)]
              (t/is (empty? errors)))))))))
