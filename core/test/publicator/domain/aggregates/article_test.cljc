(ns publicator.domain.aggregates.article-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.article :as article]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> article/blank
                (agg/with-msgs [[:agg/add-attr :root :publication/state :active]
                                [:agg/add-attr :root :publication/stream-id 1]
                                [:agg/add-attr :root :article/image-url "http://cats.com/cat.jpg"]
                                [:publication/add-translation :ru]
                                [:agg/add-attr 2 :publication.translation/state :published]
                                [:agg/add-attr 2 :publication.translation/title "some title"]
                                [:agg/add-attr 2 :publication.translation/summary "some summary"]
                                [:agg/add-attr 2 :publication.translation/published-at #inst "2019-01-01"]
                                [:agg/add-attr 2 :article.translation/content "some content"]
                                [:publication/add-related :article 1]])
                (agg/validate))]
    (t/is (agg/has-no-errors? agg))))
