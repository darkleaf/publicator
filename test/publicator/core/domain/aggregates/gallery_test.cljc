(ns publicator.core.domain.aggregates.gallery-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [datascript.core :as d]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> (agg/allocate :agg/gallery)
                (d/db-with [{:db/ident              :root
                             :publication/state     :active
                             :publication/stream-id 1
                             :gallery/image-urls    ["http://cats.com/cat.jpg"
                                                     "http://cats.com/cute.jpg"]}
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
