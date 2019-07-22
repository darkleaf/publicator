(ns publicator.domain.aggregates.author-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.author :as author]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> author/blank
                (agg/with-msgs [[:author/state :root :active]

                                [:author/add-translation "en"]
                                [:author.translation/lang "en" :en]
                                [:author.translation/first-name "en" "John"]
                                [:author.translation/last-name "en" "Doe"]

                                [:author/add-translation "ru"]
                                [:author.translation/lang "ru" :ru]
                                [:author.translation/first-name "ru" "Иван"]
                                [:author.translation/last-name "ru" "Иванов"]

                                [:author/add-stream-participation "stream-1"]
                                [:author.stream-participation/role "stream-1" :admin]
                                [:author.stream-participation/stream-id "stream-1" 1]])
                (agg/validate))]
    (t/is (agg/has-no-errors? agg))))
