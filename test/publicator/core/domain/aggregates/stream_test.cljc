(ns publicator.core.domain.aggregates.stream-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> (agg/allocate :agg/stream)
                (agg/apply-tx [{:db/ident :root
                                :stream/state :active}
                               {:stream.translation/stream :root
                                :stream.translation/lang :en
                                :stream.translation/name "Stream"}
                               {:stream.translation/stream :root
                                :stream.translation/lang  :ru
                                :stream.translation/name "Поток"}])
                (agg/validate))]
    (t/is (agg/has-no-errors? agg))))
