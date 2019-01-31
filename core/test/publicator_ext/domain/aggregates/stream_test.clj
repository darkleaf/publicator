(ns publicator-ext.domain.aggregates.stream-test
  (:require
   [publicator-ext.domain.aggregates.stream :as sut]
   [publicator-ext.domain.test.fakes :as fakes]
   [publicator-ext.utils.test.instrument :as instrument]
   [clojure.test :as t]))

(t/use-fixtures :each fakes/fixture)
(t/use-fixtures :once instrument/fixture)

(t/deftest build
  (let [tx-data [{:db/ident     :root
                  :stream/state :active}
                 {:stream.translation/stream :root
                  :stream.translation/lang   :ru
                  :stream.translation/name   "Новости"}]
        stream  (sut/build tx-data)]
    (t/is (some? stream))))
