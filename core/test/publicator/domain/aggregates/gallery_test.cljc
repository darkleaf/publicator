(ns publicator.domain.aggregates.gallery-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.gallery :as gallery]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> gallery/blank
                (agg/with-msgs [[:publication/state :add :root :active]
                                [:publication/stream-id :add :root 1]
                                [:gallery/image-urls :add :root "http://cats.com/cat.jpg"]
                                [:gallery/image-urls :add :root "http://cats.com/cute.jpg"]

                                [:publication/add-translation "ru"]
                                [:publication.translation/lang :add "ru" :ru]
                                [:publication.translation/state :add "ru" :published]
                                [:publication.translation/title :add "ru" "some title"]
                                [:publication.translation/summary :add "ru" "some summary"]
                                [:publication.translation/published-at :add "ru" #inst "2019-01-01"]

                                [:publication/add-related :article 1]])
                (agg/validate))]
    (t/is (agg/has-no-errors? agg))))
