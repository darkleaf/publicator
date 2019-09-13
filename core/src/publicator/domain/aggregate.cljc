(ns publicator.domain.aggregate
  (:require
   [publicator.util :as u]
   [datascript.core :as d]
   [datascript.parser :as d.p]))

(defprotocol Aggregate
  :extend-via-metadata true
  (rules [agg])
  (validate [agg]))

(defn extend-schema [agg ext]
  (let [schema   (merge ext (:schema agg))
        metadata (meta agg)]
    (-> agg
        (d/datoms :eavt)
        (d/init-db schema)
        (with-meta metadata))))

(defn decorate [agg decorators-map]
  (vary-meta agg #(merge-with (fn [f decorator] (partial decorator f))
                              % decorators-map)))

(defn- rules-impl [agg]
  '[[(root ?e)
     [?e :db/ident :root]]])

(defn- validate-impl [agg]
  agg)


(def blank
  (-> (d/empty-db)
      (d/db-with [[:db/add 1 :db/ident :root]])
      (with-meta
        {`rules    #'rules-impl
         `validate #'validate-impl})))

(defn root [agg]
  (d/entity agg :root))

(def ^{:arglists '([agg tx-data])} with d/db-with)

(defn- normalize-query [query]
  (cond
    (map? query)        query
    (sequential? query) (d.p/query->map query)))

(defn q [agg query & inputs]
  (let [query  (normalize-query query)
        query  (update query :in (fn [in] (concat '[$ %] in)))
        inputs (concat [agg (rules agg)] inputs)]
    (apply d/q query inputs)))

(defn has-errors? [agg]
  (boolean
   (not-empty
    (q agg '{:find [[?e ...]]
             :where [[?e :error/type _]]}))))

(defn has-no-errors? [agg]
  (not (has-errors? agg)))

(defn errors [agg]
  (let [errs (q agg '[:find [(pull ?e [*]) ...]
                      :where [?e :error/type _]])]
    (->> errs
         (map #(dissoc % :db/id))
         (set))))

(defn validate! [agg]
  (let [agg  (validate agg)
        errs (errors agg)]
    (if (not-empty errs)
      (throw (ex-info "Invalid aggregate"
                      {:errors errs}))
      agg)))

(defn- normalize-rule-form [rule-or-form]
  (cond
    (symbol? rule-or-form) (list rule-or-form '?e)
    (list? rule-or-form)   rule-or-form))

(defn required-validator [agg rule-or-form attrs]
  (let [rule-form (normalize-rule-form rule-or-form)
        query     '{:find  [?e ?a]
                    :in    [[?a ...]]
                    :where [[(missing? $ ?e ?a)]]}
        query     (update query :where #(into [rule-form] %))
        data      (q agg query attrs)
        tx-data   (for [[e a] data]
                    {:error/type   :required
                     :error/entity e
                     :error/attr   a
                     :error/rule   (first rule-form)})]
    (with agg tx-data)))

(defprotocol Predicate
  (apply-predicate [p x])
  (predicate-as-data [p]))

(defn predicate-validator [agg rule-or-form pred-map]
  (if (has-errors? agg)
    agg
    (let [rule-form (normalize-rule-form rule-or-form)
          query     '{:find  [?e ?a ?v ?pred]
                      :in    [?apply [[?a ?pred]]]
                      :where [[?e ?a ?v]
                              (not [(?apply ?pred ?v)])]}
          query     (update query :where #(into [rule-form] %))
          data      (q agg query apply-predicate pred-map)
          tx-data   (for [[e a v pred] data]
                      {:error/type   :predicate
                       :error/entity e
                       :error/attr   a
                       :error/value  v
                       :error/pred   (predicate-as-data pred)
                       :error/rule   (first rule-form)})]
      (with agg tx-data))))

(defn query-validator [agg rule-or-form query predicate]
  (if (has-errors? agg)
    agg
    (let [rule-form (normalize-rule-form rule-or-form)
          entities  (q agg [:find '[?e ...] :where rule-form])
          query     (-> query
                        (normalize-query)
                        (assoc :in '[?e]))
          tx-data   (for [e    entities
                          :let [res (q agg query e)]]
                      (if (not (predicate res))
                        {:error/type   :query
                         :error/entity e
                         :error/result res
                         :error/pred   (predicate-as-data predicate)
                         :error/rule   (first rule-form)
                         :error/query  query}))]
      (with agg tx-data))))

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
