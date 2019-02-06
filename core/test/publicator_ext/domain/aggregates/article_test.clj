(ns publicator-ext.domain.aggregates.article-test
  (:require
   [publicator-ext.domain.aggregates.article :as sut]
   [publicator-ext.domain.abstractions.scaffolding :as scaffolding]
   [publicator-ext.domain.abstractions.instant :as instant]
   [clojure.test :as t]))

(t/use-fixtures :each scaffolding/setup)

(t/deftest build
  (let [tx-data [{:db/ident                :root
                  :publication/state       :active
                  :publication/related-ids [1 2 3]
                  :publication/stream-id   1
                  :article/image-url       "some url"}
                 {:publication.translation/publication  :root
                  :publication.translation/state        :published
                  :publication.translation/lang         :en
                  :publication.translation/title        "some text"
                  :publication.translation/summary      "some text"
                  :publication.translation/published-at (instant/now)
                  :article.translation/content          "some text"}]
        article (sut/build tx-data)]
    (t/is (some? article))))
