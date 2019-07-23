(ns publicator.domain.aggregates.author-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.author :as author]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> author/blank
                (agg/with-msgs [[:author/state :add :root :active]

                                [:author/add-translation "en"]
                                [:author.translation/lang :add "en" :en]
                                [:author.translation/first-name :add "en" "John"]
                                [:author.translation/last-name :add "en" "Doe"]

                                [:author/add-translation "ru"]
                                [:author.translation/lang :add "ru" :ru]
                                [:author.translation/first-name :add "ru" "Иван"]
                                [:author.translation/last-name :add "ru" "Иванов"]

                                [:author/add-stream-participation "stream-1"]
                                [:author.stream-participation/role :add "stream-1" :admin]
                                [:author.stream-participation/stream-id :add "stream-1" 1]])
                (agg/validate))]
    (t/is (agg/has-no-errors? agg))))
