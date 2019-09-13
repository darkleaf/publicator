(ns publicator.domain.aggregates.stream-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.stream :as stream]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> stream/blank
                (agg/with [{:db/ident :root
                            :stream/state :active}
                           {:stream.translation/stream :root
                            :stream.translation/lang :en
                            :stream.translation/name "Stream"}
                           {:stream.translation/stream :root
                            :stream.translation/lang  :ru
                            :stream.translation/name "Поток"}])
                (agg/validate))]
    (t/is (agg/has-no-errors? agg))))
