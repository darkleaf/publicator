(ns publicator-ext.domain.aggregates.gallery-test
  (:require
   [publicator-ext.domain.aggregates.gallery :as sut]
   [publicator-ext.domain.test.fakes :as fakes]
   [publicator-ext.utils.test.instrument :as instrument]
   [publicator-ext.domain.abstractions.aggregate :as aggregate]
   [publicator-ext.domain.abstractions.instant :as instant]
   [clojure.spec.alpha :as s]
   [clojure.test :as t]))

(t/use-fixtures :each fakes/fixture)
(t/use-fixtures :once instrument/fixture)

(t/deftest build
  (let [params             {:publication/state       :active
                            :publication/related-ids [1 2 3]
                            :publication/stream-id   1
                            :gallery/image-urls      ["some url"]}
        translation-params {:publication.translation/state        :published
                            :publication.translation/lang         :en
                            :publication.translation/title        "some text"
                            :publication.translation/summary      "some text"
                            :publication.translation/published-at (instant/now)}
        gallery            (-> (sut/build params)
                               (sut/add-translation translation-params))]
    (t/is (some? gallery))))
