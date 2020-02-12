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


(md/decorate agg/allowed-attribute? :allowed-attribute?-test/agg
  (fn [super type attr]
    (or (super type attr)
        (#{:allowed-attribute?-test/attr} attr))))

(t/deftest allowed-attribute?
  (t/is (agg/allowed-attribute? :allowed-attribute?-test/agg :allowed-attribute?-test/attr))
  (t/is (not (agg/allowed-attribute? :allowed-attribute?-test/agg :wrong)))

  (t/is (agg/allowed-attribute? :allowed-attribute?-test/agg :agg/id))
  (t/is (agg/allowed-attribute? :allowed-attribute?-test/agg :agg/some-attr))

  (t/is (agg/allowed-attribute? :allowed-attribute?-test/agg :db/ident))
  (t/is (agg/allowed-attribute? :allowed-attribute?-test/agg :db/some-attr))

  (t/is (agg/allowed-attribute? :allowed-attribute?-test/agg :error/type))
  (t/is (agg/allowed-attribute? :allowed-attribute?-test/agg :error/some-attr)))


(md/decorate agg/schema :becomes-test/agg-from
  (fn [super type]
    (assoc (super type)
           :becomes-test/many {:db/cardinality :db.cardinality/many})))

(md/decorate agg/allowed-attribute? :becomes-test/agg-from
  (fn [super type attr]
    (or (super type attr)
        (#{:becomes-test/attr-1
           :becomes-test/attr-2
           :becomes-test/many} attr))))

(md/decorate agg/schema :becomes-test/agg-to
  (fn [super type]
    (assoc (super type)
           :becomes-test/many {:db/cardinality :db.cardinality/many})))

(md/decorate agg/allowed-attribute? :becomes-test/agg-to
  (fn [super type attr]
    (or (super type attr)
        (#{:becomes-test/attr-1
           :becomes-test/many} attr))))

(t/deftest becomes
  (let [agg-from (-> (agg/allocate :becomes-test/agg-from)
                     (d/db-with [[:db/add :root :becomes-test/attr-1 :value-1]
                                 [:db/add :root :becomes-test/attr-2 :value-2]
                                 [:db/add :root :becomes-test/many 1]
                                 [:db/add :root :becomes-test/many 2]]))
        agg-to   (agg/becomes agg-from :becomes-test/agg-to)]
    (t/is (= :becomes-test/agg-to (u/type agg-to)))
    (t/is (= [(d/datom 1 :becomes-test/attr-1 :value-1)
              #_(d/datom 1 :becomes-test/attr-2 :value-2)
              (d/datom 1 :becomes-test/many 1)
              (d/datom 1 :becomes-test/many 2)
              (d/datom 1 :db/ident :root)]
             (d/datoms agg-to :eavt)))))



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
