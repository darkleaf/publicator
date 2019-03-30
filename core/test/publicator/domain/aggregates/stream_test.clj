(ns publicator.domain.aggregates.stream-test
  (:require
   [publicator.domain.aggregates.stream :as stream]
   [publicator.domain.aggregate :as agg]
   [publicator.domain.abstractions.test-impl.scaffolding :as scaffolding]
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
        stream  (-> (agg/build stream/spec)
                    (agg/change tx-data agg/allow-everething))
        errors  (agg/validate stream)]
    (t/is (empty? errors))))
