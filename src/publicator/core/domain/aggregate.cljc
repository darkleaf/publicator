(ns publicator.core.domain.aggregate
  (:require
   [publicator.util :as u]
   [datascript.core :as d]
   [darkleaf.multidecorators :as md]))

(defn- initial-schema [type]
  {:error/entity {:db/valueType :db.type/ref}})

(defonce schema (md/multi (fn [type] type)
                          #'initial-schema))

(defn allocate [type]
  (-> (d/empty-db (schema type))
      (with-meta {:type type})
      (d/db-with [[:db/add 1 :db/ident :root]])))

(defn remove-errors [agg]
  (let [errors  (d/q '[:find [?e ...]
                       :where [?e :error/entity _]]
                     agg)
        tx-data (for [error errors]
                  [:db.fn/retractEntity error])]
    (d/db-with agg tx-data)))

(defonce validate (md/multi (fn [agg] (u/type agg))
                            #'remove-errors))

(defn has-errors? [agg]
  (d/q '[:find ?e .
         :where [?e :error/entity _]]
       agg))

(defn has-no-errors? [agg]
  (not (has-errors? agg)))

(defn check-errors! [agg]
  (if (has-errors? agg)
    (throw (ex-info "Invalid aggregate"
                    {:agg agg}))
    agg))

;; (defn- normalize-rule-form [rule-or-form]
;;   (cond
;;     (symbol? rule-or-form) (list rule-or-form '?e)
;;     (list? rule-or-form)   rule-or-form))

;; (defn ^{:style/indent :defn} required-validator [agg rule-or-form attrs]
;;   (let [rule-form (normalize-rule-form rule-or-form)
;;         query     '{:find  [?e ?a]
;;                     :in    [[?a ...]]
;;                     :where [[(missing? $ ?e ?a)]]}
;;         query     (update query :where #(into [rule-form] %))
;;         data      (q agg query attrs)
;;         tx-data   (for [[e a] data]
;;                     {:error/type   :required
;;                      :error/entity e
;;                      :error/attr   a
;;                      :error/rule   (first rule-form)})]
;;     (apply-tx agg tx-data)))

(defprotocol Predicate
  (apply-predicate [p x])
  (predicate-as-data [p]))

(defn ^{:style/indent :defn} predicate-validator [agg pred-map]
  (if (has-errors? agg)
    agg
    (let [query   '[:find ?e ?a ?v ?pred
                    :in $ ?apply [[?a ?pred]]
                    :where
                    [?e ?a ?v]
                    (not [(?apply ?pred ?v)])]
          data    (d/q query agg apply-predicate pred-map)
          tx-data (for [[e a v pred] data]
                    {:error/type   :predicate
                     :error/entity e
                     :error/attr   a
                     :error/value  v
                     :error/pred   (predicate-as-data pred)})]
      (d/db-with agg tx-data))))


;; (defn ^{:style/indent :defn} query-validator [agg rule-or-form query predicate]
;;   (if (has-errors? agg)
;;     agg
;;     (let [rule-form (normalize-rule-form rule-or-form)
;;           entities  (q agg [:find '[?e ...] :where rule-form])
;;           query*    (-> query
;;                         (normalize-query)
;;                         (assoc :in '[?e]))
;;           tx-data   (for [e    entities
;;                           :let [res (q agg query* e)]]
;;                       (if (not (predicate res))
;;                         (cond-> {:error/type   :query
;;                                  :error/entity e
;;                                  :error/pred   (predicate-as-data predicate)
;;                                  :error/rule   (first rule-form)
;;                                  :error/query  query}
;;                           (some? res) (assoc :error/result res))))]
;;       (apply-tx agg tx-data))))

(extend-protocol Predicate
  #?(:clj  clojure.lang.PersistentHashSet
     :cljs cljs.core/PersistentHashSet)
  (apply-predicate [p x] (p x))
  (predicate-as-data [p] p)

  #?(:clj  clojure.lang.Var
     :cljs cljs.core/Var)
  (apply-predicate [p x] (p x))
  (predicate-as-data [p] (symbol p))

  #?(:clj  java.util.regex.Pattern
     :cljs js/RegExp)
  (apply-predicate [p x]
    (and (string? x)
         (re-matches p x)))
  (predicate-as-data [p] (str p)))
