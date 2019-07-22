(ns publicator.domain.aggregates.article-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.article :as article]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> article/blank
                (agg/with-msgs [[:publication/state :root :active]
                                [:publication/stream-id :root 1]
                                [:article/image-url :root "http://cats.com/cat.jpg"]

                                [:publication/add-translation "ru"]
                                [:publication.translation/lang "ru" :ru]
                                [:publication.translation/state "ru" :published]
                                [:publication.translation/title "ru" "some title"]
                                [:publication.translation/summary "ru" "some summary"]
                                [:publication.translation/published-at "ru" #inst "2019-01-01"]
                                [:article.translation/content "ru" "some content"]

                                [:publication/add-related :article 1]])
                (agg/validate))]
    (t/is (agg/has-no-errors? agg))))
