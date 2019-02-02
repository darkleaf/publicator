(ns publicator-ext.domain.aggregates.author-test
  (:require
   [publicator-ext.domain.aggregates.author :as sut]
   [publicator-ext.domain.test.fakes :as fakes]
   [publicator-ext.domain.abstractions.aggregate :as aggregate]
   [clojure.test :as t]))

(t/use-fixtures :each fakes/fixture)

(t/deftest build
  (let [user-id 1
        tx-data [{:db/ident     :root
                  :author/state :active}
                 {:author.translation/author     :root
                  :author.translation/lang       :en
                  :author.translation/first-name "John"
                  :author.translation/last-name  "Doe"}
                 {:author.stream-participation/author    :root
                  :author.stream-participation/stream-id 1
                  :author.stream-participation/role      :regular}]
        author  (sut/build user-id tx-data)]
    (t/is (some? author))))
