(ns publicator.core.domain.aggregates.stream-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.stream :as stream]
   [datascript.core :as d]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> (agg/allocate)
                (d/db-with [{:db/ident     :root
                             :stream/state :active}
                            {:stream.translation/stream :root
                             :stream.translation/lang   :en
                             :stream.translation/name   "Stream"}
                            {:stream.translation/stream :root
                             :stream.translation/lang   :ru
                             :stream.translation/name   "Поток"}])
                (stream/validate))]
    (t/is (agg/has-no-errors? agg))))
