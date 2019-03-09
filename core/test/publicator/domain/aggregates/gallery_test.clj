(ns publicator.domain.aggregates.gallery-test
  (:require
   [publicator.domain.aggregates.gallery :as gallery]
   [publicator.domain.aggregate :as agg]
   [publicator.domain.abstractions.scaffolding :as scaffolding]
   [publicator.domain.abstractions.instant :as instant]
   [clojure.test :as t]))

(t/use-fixtures :each scaffolding/setup)

(t/deftest build
  (let [tx-data [{:db/ident              :root
                  :publication/stream-id 1
                  :gallery/image-urls    ["some url"]}
                 {:publication.translation/publication  :root
                  :publication.translation/state        :published
                  :publication.translation/lang         :en
                  :publication.translation/title        "some text"
                  :publication.translation/summary      "some text"
                  :publication.translation/published-at (instant/*now*)}
                 {:publication.related/publication :root
                  :publication.related/id          1
                  :publication.related/type        :article}]
        gallery (agg/build gallery/spec tx-data)]
    (t/is (some? gallery))))
