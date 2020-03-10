(ns publicator.core.domain.aggregates.author-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.author :as author]
   [datascript.core :as d]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> (agg/allocate)
                (d/db-with [{:db/ident     :root
                             :author/state :active}
                            {:author.translation/author     :root
                             :author.translation/lang       :en
                             :author.translation/first-name "John"
                             :author.translation/last-name  "Doe"}
                            {:author.translation/author     :root
                             :author.translation/lang       :ru
                             :author.translation/first-name "Иван"
                             :author.translation/last-name  "Иванов"}
                            {:author.stream-participation/author    :root
                             :author.stream-participation/role      :admin
                             :author.stream-participation/stream-id 1}])
                (author/validate))]
    (t/is (agg/has-no-errors? agg))))
