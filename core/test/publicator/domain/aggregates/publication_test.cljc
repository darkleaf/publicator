(ns publicator.domain.aggregates.publication-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.publication :as publication]
   [clojure.test :as t]))

(t/deftest without-errors
  (let [agg (-> publication/blank
                (agg/with [{:db/ident              :root
                            :publication/state     :active
                            :publication/stream-id 1}
                           {:publication.translation/publication  :root
                            :publication.translation/lang         :ru
                            :publication.translation/state        :published
                            :publication.translation/title        "some title"
                            :publication.translation/summary      "some summary"
                            :publication.translation/published-at #inst "2019-01-01"}
                           {:publication.related/publication :root
                            :publication.related/id          1
                            :publication.related/type        :article}])
                (agg/validate))]
    (t/is (agg/has-no-errors? agg))))
