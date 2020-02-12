(ns publicator.core.domain.aggregate-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.util :as u]
   [darkleaf.multidecorators :as md]
   [datascript.core :as d]
   [clojure.test :as t]))

(md/decorate agg/schema :allocate-test/agg
  (fn [super type]
    (assoc (super type)
           :allocate-test/many {:db/cardinality :db.cardinality/many})))

(t/deftest allocate
  (let [agg (agg/allocate :allocate-test/agg)]
    (t/is (some? agg))
    (t/is (= :allocate-test/agg (u/type agg)))
    (t/is (-> agg :schema (contains? :allocate-test/many)))
    (t/is (= 1 (d/q '[:find ?e .
                      :where [?e :db/ident :root]]
                    agg)))))

(t/deftest remove-errors
  (let [agg (-> (agg/allocate :remove-errors-test/agg)
                (d/db-with [{:error/entity :root}]))]
    (t/is (-> agg agg/has-errors?))
    (t/is (-> agg agg/remove-errors agg/has-no-errors?))))


;; (md/decorate agg/validate :agg/test-agg
;;   (fn [super agg]
;;     (-> (super agg)
;;         (agg/required-validator 'root
;;           #{:test-agg/attr})
;;         (agg/predicate-validator 'root
;;           {:test-agg/attr #'int?})
;;         (agg/query-validator 'root
;;           '[:find ?v .
;;             :where [?e :test-agg/attr2 ?v]]
;;           #'int?))))


;; (md/decorate agg/allowed-attribute? :agg/test-agg
;;   (fn [super type attr]
;;     (or (super type attr)
;;         (#{:test-agg/attr
;;            :test-agg/many} attr))))



;; (t/deftest validate
;;   (let [agg (agg/allocate :agg/test-agg)]
;;     (t/testing "required"
;;       (t/is (= (agg/apply-tx agg [{:error/rule   'root
;;                                    :error/entity :root
;;                                    :error/attr   :test-agg/attr
;;                                    :error/type   :required}])
;;                (agg/validate agg))))
;;     (t/testing "predicate"
;;       (let [agg (-> agg
;;                     (agg/apply-tx [[:db/add :root :test-agg/attr :wrong]]))]
;;         (t/is (= (agg/apply-tx agg [{:error/rule   'root
;;                                      :error/entity :root
;;                                      :error/attr   :test-agg/attr
;;                                      :error/value  :wrong
;;                                      :error/pred   `int?
;;                                      :error/type   :predicate}])
;;                  (agg/validate agg)))))
;;     (t/testing "query"
;;       (let [agg (-> agg
;;                     (agg/apply-tx [[:db/add :root :test-agg/attr 1]]))]
;;         (t/is (= (agg/apply-tx agg [{:error/rule   'root
;;                                      :error/entity :root
;;                                      :error/pred   `int?
;;                                      :error/query  '[:find ?v .
;;                                                      :where [?e :test-agg/attr2 ?v]]
;;                                      :error/type   :query}])
;;                  (agg/validate agg)))))
;;     (t/testing "clear previous errors"
;;       (let [agg (-> agg
;;                     (agg/validate)
;;                     (agg/apply-tx [[:db/add :root :test-agg/attr :wrong]]))]
;;         (t/is (= (agg/apply-tx agg [[:db/retractEntity 2]
;;                                     {:error/rule   'root
;;                                      :error/entity :root
;;                                      :error/attr   :test-agg/attr
;;                                      :error/value  :wrong
;;                                      :error/pred   `int?
;;                                      :error/type   :predicate}])
;;                  (agg/validate agg)))))))
