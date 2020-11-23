(ns publicator.core.domain.aggregates.author-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.author :as author]
   [datascript.core :as d]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> (agg/build {:translation/root              :root
                            :translation/lang              :en
                            :author.translation/first-name "John"
                            :author.translation/last-name  "Doe"}
                           {:translation/root              :root
                            :translation/lang              :ru
                            :author.translation/first-name "Иван"
                            :author.translation/last-name  "Иванов"}
                           {:author.achivement/root        :root
                            :author.achivement/kind        :star
                            :author.achivement/assigner-id 42})
                (author/validate))]
    (t/is (agg/has-no-errors? agg))))
