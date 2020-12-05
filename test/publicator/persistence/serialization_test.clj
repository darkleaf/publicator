(ns publicator.persistence.serialization-test
  (:require
   [clojure.test :as t]
   [datascript.core :as d]
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.translation :as translation]
   [publicator.persistence.serialization :as sut]))

(swap! agg/schema merge
       {:st/state             {:agg/predicate keyword?}
        :st.translation/title {:agg/predicate string?}
        :st.translation/tags  {:db/cardinality :db.cardinality/many}
        :st.nested/root       {:db/valueType :db.type/ref}
        :st.nested/attr-1     {:agg/predicate string?}
        :st.nested/attr-2     {:agg/predicate int?}})

(t/deftest ok
  (let [agg (-> (agg/build {:db/ident :root
                            :st/state :ok}
                           {:translation/root     :root
                            :translation/lang     :en
                            :st.translation/title "title"
                            :st.translation/tags  #{:tag-1 :tag-2}}
                           {:st.nested/root   :root
                            :st.nested/attr-1 "str-1"
                            :st.nested/attr-2 1}
                           {:st.nested/root   :root
                            :st.nested/attr-1 "str-2"
                            :st.nested/attr-2 2})
                (agg/validate)
                (translation/validate)
                (agg/check-errors))
        row {"st/state"                :ok
             "en$db/id"                2
             "en$st.translation/title" "title"
             "en$st.translation/tags"  [:tag-1 :tag-2]
             "es#st.nested/root"       [3 4]
             "vs#st.nested/root"       [1 1]
             "es#st.nested/attr-1"     [3 4]
             "vs#st.nested/attr-1"     ["str-1" "str-2"]
             "es#st.nested/attr-2"     [3 4]
             "vs#st.nested/attr-2"     [1 2]}]
    (t/is (= row (sut/agg->row agg)))
    (t/is (= agg (sut/row->agg row)))))
