(ns publicator.core.domain.aggregates.publication-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.publication :as publication]
   [datascript.core :as d]
   [clojure.test :as t]))

(t/deftest article-has-no-errors
  (let [agg (-> (agg/build {:db/ident               :root
                            :publication/type       :article
                            :publication/state      :active
                            :publication/author-id  1
                            :publication/related-id #{2 3}
                            :article/image-url      "http://cats.com/cat.jpg"}
                           {:publication.translation/publication  :root
                            :publication.translation/lang         :ru
                            :publication.translation/state        :published
                            :publication.translation/title        "some title"
                            :publication.translation/summary      "some summary"
                            :publication.translation/published-at #inst "2019-01-01"
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
                           {:publication.translation/publication  :root
                            :publication.translation/lang         :ru
                            :publication.translation/state        :published
                            :publication.translation/title        "some title"
                            :publication.translation/summary      "some summary"
                            :publication.translation/tag          #{"animal" "cat"}
                            :publication.translation/published-at #inst "2019-01-01"})
                (publication/validate))]
    (t/is (agg/has-no-errors? agg))))
