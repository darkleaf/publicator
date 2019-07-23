(ns publicator.domain.aggregates.article-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.article :as article]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> article/blank
                (agg/with-msgs [[:publication/state :add :root :active]
                                [:publication/stream-id :add :root 1]
                                [:article/image-url :add :root "http://cats.com/cat.jpg"]

                                [:publication/add-translation "ru"]
                                [:publication.translation/lang :add "ru" :ru]
                                [:publication.translation/state :add "ru" :published]
                                [:publication.translation/title :add "ru" "some title"]
                                [:publication.translation/summary :add "ru" "some summary"]
                                [:publication.translation/published-at :add "ru" #inst "2019-01-01"]
                                [:article.translation/content :add "ru" "some content"]

                                [:publication/add-related :article 1]])
                (agg/validate))]
    (t/is (agg/has-no-errors? agg))))
