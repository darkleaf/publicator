(ns publicator.domain.aggregates.gallery-test
  (:require
   [publicator.domain.aggregates.gallery :as sut]
   [publicator.domain.abstractions.scaffolding :as scaffolding]
   [publicator.domain.abstractions.instant :as instant]
   [clojure.test :as t]))

(t/use-fixtures :each scaffolding/setup)

(t/deftest build
  (let [tx-data [{:db/ident                :root
                  :publication/state       :active
                  :publication/related-ids [1 2 3]
                  :publication/stream-id   1
                  :gallery/image-urls      ["some url"]}
                 {:publication.translation/publication  :root
                  :publication.translation/state        :published
                  :publication.translation/lang         :en
                  :publication.translation/title        "some text"
                  :publication.translation/summary      "some text"
                  :publication.translation/published-at (instant/*now*)}]
        gallery (sut/build tx-data)]
    (t/is (some? gallery))))
