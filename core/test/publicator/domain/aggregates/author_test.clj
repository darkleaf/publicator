(ns publicator.domain.aggregates.author-test
  (:require
   [publicator.domain.aggregates.author :as author]
   [publicator.domain.aggregate :as agg]
   [publicator.domain.abstractions.scaffolding :as scaffolding]
   [clojure.test :as t]))

(t/use-fixtures :each scaffolding/setup)

(t/deftest build
  (let [user-id 1
        tx-data [{:db/ident :root
                  :root/id  user-id}
                 {:author.translation/author     :root
                  :author.translation/lang       :en
                  :author.translation/first-name "John"
                  :author.translation/last-name  "Doe"}
                 {:author.translation/author     :root
                  :author.translation/lang       :ru
                  :author.translation/first-name "Иван"
                  :author.translation/last-name  "Иванов"}
                 {:author.stream-participation/author    :root
                  :author.stream-participation/stream-id 1
                  :author.stream-participation/role      :regular}]
        author  (agg/build! author/spec tx-data)]
    (t/is (some? author))))
