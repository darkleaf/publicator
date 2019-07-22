(ns publicator.domain.aggregates.stream-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.stream :as stream]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> stream/blank
                (agg/with-msgs [[:stream/state :root :active]

                                [:stream/add-translation "en"]
                                [:stream.translation/lang "en" :en]
                                [:stream.translation/name "en" "Stream"]

                                [:stream/add-translation "ru"]
                                [:stream.translation/lang "ru" :ru]
                                [:stream.translation/name "ru" "Поток"]])
                (agg/validate))]
    (t/is (agg/has-no-errors? agg))))
