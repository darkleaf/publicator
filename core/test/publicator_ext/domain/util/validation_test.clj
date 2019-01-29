(ns publicator-ext.domain.util.validation-test
  (:require
   [publicator-ext.domain.util.validation :as sut]
   [publicator-ext.utils.test.instrument :as instrument]
   [datascript.core :as d]
   [clojure.test :as t]))

(t/use-fixtures :once instrument/fixture)

(t/deftest build
  (let [validator (fn [aggregate]
                    (-> (sut/begin aggregate)
                        (sut/attributes '[[(entity ?e)
                                           [?e _ _]]]
                                        [[:req :attr/keyword keyword?]
                                         [:req :attr/int int?]
                                         [:req :attr/int < 10]])
                        ;; check doubles
                        (sut/attributes '[[(entity ?e)
                                           [?e _ _]]]
                                        [[:req :attr/keyword keyword?]
                                         [:req :attr/int int?]
                                         [:req :attr/int < 10]])
                        (sut/complete)))]
    (t/testing "no errors"
      (let [aggregate (-> (d/empty-db)
                          (d/db-with [{:db/id        1
                                       :attr/keyword :value
                                       :attr/int     1}]))
            errors    (validator aggregate)]
        (t/is (empty? errors))))
    (t/testing "errors"
      (let [aggregate (-> (d/empty-db)
                          (d/db-with [{:db/id      1
                                       :attr/other :some-val
                                       :attr/int   100}]))
            errors    (validator aggregate)]
        ;; todo: better checks
        (t/is (not-empty errors))))))
