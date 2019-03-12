(ns publicator.domain.aggregates.article-test
  (:require
   [publicator.domain.aggregates.article :as article]
   [publicator.domain.aggregate :as agg]
   [publicator.domain.abstractions.test-impl.scaffolding :as scaffolding]
   [publicator.domain.abstractions.instant :as instant]
   [clojure.test :as t]))

(t/use-fixtures :each scaffolding/setup)

(t/deftest build
  (let [tx-data [{:db/ident              :root
                  :publication/stream-id 1
                  :publication/state     :active
                  :article/image-url     "some url"}
                 {:publication.translation/publication  :root
                  :publication.translation/state        :published
                  :publication.translation/lang         :en
                  :publication.translation/title        "some text"
                  :publication.translation/summary      "some text"
                  :publication.translation/published-at (instant/*now*)
                  :article.translation/content          "some text"}
                 {:publication.related/publication :root
                  :publication.related/id          1
                  :publication.related/type        :article}]
        article (agg/build! article/spec tx-data)]
    (t/is (some? article))))
