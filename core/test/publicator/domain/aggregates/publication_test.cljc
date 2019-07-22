(ns publicator.domain.aggregates.publication-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.publication :as publication]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> publication/blank
                (agg/with-msgs [[:publication/state :root :active]
                                [:publication/stream-id :root 1]

                                [:publication/add-translation "ru"]
                                [:publication.translation/lang "ru" :ru]
                                [:publication.translation/state "ru" :published]
                                [:publication.translation/title "ru" "some title"]
                                [:publication.translation/summary "ru" "some summary"]
                                [:publication.translation/published-at "ru" #inst "2019-01-01"]

                                [:publication/add-related :article 1]])
                (agg/validate))]
    (t/is (agg/has-no-errors? agg))))
