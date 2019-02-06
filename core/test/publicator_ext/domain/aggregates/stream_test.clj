(ns publicator-ext.domain.aggregates.stream-test
  (:require
   [publicator-ext.domain.aggregates.stream :as sut]
   [publicator-ext.domain.abstractions.scaffolding :as scaffolding]
   [clojure.test :as t]))

(t/use-fixtures :each scaffolding/setup)

(t/deftest build
  (let [tx-data [{:db/ident     :root
                  :stream/state :active}
                 {:stream.translation/stream :root
                  :stream.translation/lang   :en
                  :stream.translation/name   "News"}
                 {:stream.translation/stream :root
                  :stream.translation/lang   :ru
                  :stream.translation/name   "Новости"}]
        stream  (sut/build tx-data)]
    (t/is (some? stream))))
