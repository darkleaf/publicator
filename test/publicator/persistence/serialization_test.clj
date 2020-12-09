(ns publicator.persistence.serialization-test
  (:require
   [clojure.test :as t]
   [datascript.core :as d]
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.translation :as translation]
   [publicator.persistence.serialization :as sut]))

(swap! agg/schema merge
       {:st/state             {:agg/predicate           #{:ok :wrong}
                               :persistence.value/read  keyword
                               :persistence.value/write name}
        :st/tags              {:agg/predicate           simple-keyword?
                               :db/cardinality          :db.cardinality/many
                               :persistence.value/read  keyword
                               :persistence.value/write name}
        :st.translation/title {:agg/predicate string?}
        :st.translation/tags  {:agg/predicate           simple-keyword?
                               :db/cardinality          :db.cardinality/many
                               :persistence.value/read  keyword
                               :persistence.value/write name}
        :st.nested/root       {:db/valueType :db.type/ref}
        :st.nested/attr       {:agg/predicate string?}
        :st.nested/tags       {:agg/predicate           int?
                               :db/cardinality          :db.cardinality/many
                               :persistence.value/read  #(Long/parseLong %)
                               :persistence.value/write str}})

(t/deftest ok
  (let [agg (-> (agg/build {:db/ident :root
                            :st/state :ok
                            :st/tags  #{:a :b}}
                           {:translation/root     :root
                            :translation/lang     :en
                            :st.translation/title "title"
                            :st.translation/tags  #{:tag-1 :tag-2}}
                           {:st.nested/root :root
                            :st.nested/attr "str-1"
                            :st.nested/tags #{1 2}}
                           {:st.nested/root :root
                            :st.nested/attr "str-2"
                            :st.nested/tags #{3 4}})
                (agg/validate)
                (translation/validate)
                (agg/check-errors))
        row {"st/state"                "ok"
             "st/tags"                 ["a" "b"]
             "en$db/id"                2
             "en$st.translation/title" "title"
             "en$st.translation/tags"  ["tag-1" "tag-2"]
             "es#st.nested/root"       [3 4]
             "vs#st.nested/root"       [1 1]
             "es#st.nested/attr"       [3 4]
             "vs#st.nested/attr"       ["str-1" "str-2"]
             "es#st.nested/tags"       [3 4 4 3]
             "vs#st.nested/tags"       ["2" "4" "3" "1"]}]
    (t/is (= row (sut/agg->row agg)))
    (t/is (= agg (sut/row->agg row)))))
