(ns publicator.core.domain.aggregate
  (:require
   [publicator.util :as u]
   [datascript.core :as d]
   [datascript.db :as d.db]
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
  (->> (d/datoms agg :aevt :error/entity)
       (map (fn [{:keys [e]}] [:db.fn/retractEntity e]))
       (d/db-with agg)))

(defonce validate (md/multi (fn [agg] (u/type agg))
                            #'remove-errors))

(defn has-errors? [agg]
  (boolean (seq (d/datoms agg :aevt :error/entity))))

(defn has-no-errors? [agg]
  (not (has-errors? agg)))

(defn check-errors! [agg]
  (if (has-errors? agg)
    (throw (ex-info "Invalid aggregate"
                    {:agg agg}))
    agg))

(defn- entities-by-tag [agg tag]
  "the tag is an ident, ref, reveresed ref or attr-value pair"
  (cond
    (vector? tag)           (->> (apply d/datoms agg :avet tag)
                                 (map :e))
    (not (keyword? tag))    nil
    (d.db/reverse-ref? tag) (->> (d/datoms agg :avet (d.db/reverse-ref tag))
                                 (map :e))
    (d.db/ref? agg tag)     (->> (d/datoms agg :avet tag)
                                 (map :v))
    :ident                  (->> (d/datoms agg :avet :db/ident tag)
                                 (map :e))))

(defn required-validator [agg desc]
  (let [tx-data (for [[tag attrs] desc
                      e           (entities-by-tag agg tag)
                      a           attrs
                      :when       (empty? (d/datoms agg :eavt e a))]
                  {:error/type   :required
                   :error/entity e
                   :error/attr   a})]
    (d/db-with agg tx-data)))

(defprotocol Predicate
  (apply-predicate [p x])
  (predicate-as-data [p]))

(defn predicate-validator [agg pred-map]
  (if (has-errors? agg)
    agg
    (let [tx-data (for [[a pred] pred-map
                        [e _ v]     (d/datoms agg :aevt a)
                        :when       (not (apply-predicate pred v))]
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
