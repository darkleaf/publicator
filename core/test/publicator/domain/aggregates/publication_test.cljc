(ns publicator.domain.aggregates.publication-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.publication :as publication]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> publication/blank
                (agg/with-msgs [[:agg/add-attr :root :publication/state :active]
                                [:agg/add-attr :root :publication/stream-id 1]
                                [:publication/add-translation :ru]
                                [:agg/add-attr 2 :publication.translation/state :published]
                                [:agg/add-attr 2 :publication.translation/title "some title"]
                                [:agg/add-attr 2 :publication.translation/summary "some summary"]
                                [:agg/add-attr 2 :publication.translation/published-at #inst "2019-01-01"]
                                [:publication/add-related :article 1]])
                (agg/validate))]
    (t/is (agg/has-no-errors? agg))))
