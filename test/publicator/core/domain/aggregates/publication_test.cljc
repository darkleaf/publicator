(ns publicator.core.domain.aggregates.publication-test
  (:require
   [cljc.java-time.instant :as time.instant]
   [clojure.test :as t]
   [datascript.core :as d]
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.publication :as publication]))

(t/deftest article-has-no-errors
  (let [agg (-> (agg/build {:db/ident               :root
                            :publication/type       :article
                            :publication/state      :active
                            :publication/author-id  1
                            :publication/related-id #{2 3}
                            :article/image-url      "http://cats.com/cat.jpg"}
                           {:translation/root                     :root
                            :translation/lang                     :ru
                            :publication.translation/state        :published
                            :publication.translation/title        "some title"
                            :publication.translation/summary      "some summary"
                            :publication.translation/published-at (time.instant/now)
                            :publication.translation/tag          #{"animal" "cat"}
                            :article.translation/content          "some content"})
                (publication/validate))]
    (t/is (agg/has-no-errors? agg))))

(t/deftest gallery-has-no-errors
  (let [agg (-> (agg/build {:db/ident               :root
                            :publication/type       :gallery
                            :publication/state      :active
                            :publication/author-id  1
                            :publication/related-id #{2 3}
                            :gallery/image-url      #{"http://cats.com/cat.jpg"
                                                      "http://cats.com/cute.jpg"}}
                           {:translation/root                     :root
                            :translation/lang                     :ru
                            :publication.translation/state        :published
                            :publication.translation/title        "some title"
                            :publication.translation/summary      "some summary"
                            :publication.translation/tag          #{"animal" "cat"}
                            :publication.translation/published-at (time.instant/now)})
                (publication/validate))]
    (t/is (agg/has-no-errors? agg))))
