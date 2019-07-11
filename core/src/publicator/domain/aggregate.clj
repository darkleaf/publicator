(ns publicator.domain.aggregate
  (:require
   [datascript.core :as d]
   [datascript.parser :as d.p]
   [clojure.core.match :as m]))

(defprotocol Aggregate
  :extend-via-metadata true
  (rules [agg])
  (validators [agg])
  (msg->tx [agg msg]))

(defn extend-schema [agg ext]
  (let [schema   (merge ext (:schema agg))
        metadata (meta agg)]
    (-> agg
        (d/datoms :eavt)
        (d/init-db schema)
        (with-meta metadata))))

(defn decorate [agg decorators-map]
  (vary-meta agg #(merge-with partial decorators-map %)))

(defn- rules-impl [agg]
  '[[(root ?e)
     [?e :db/ident :root]]])

(defn- validators-impl [agg]
  [])

(defn- msg->tx-impl [agg msg]
  (m/match msg
    {:type   :add-attr
     :entity e
     :attr   a
     :value  v}
    [[:db/add e a v]]))

(def blank
  (-> (d/empty-db)
      (d/db-with [[:db/add 1 :db/ident :root]])
      (with-meta
        {`rules      #'rules-impl
         `validators #'validators-impl
         `msg->tx    #'msg->tx-impl})))

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

(defn apply-msg [agg msg]
  (with agg (msg->tx agg msg)))

(defn has-errors? [agg]
  (not-empty
   (q agg '{:find [[?e ...]]
            :where [[?e :error/type _]]})))

(defn validate [agg]
  (reduce
   (fn [agg validator]
     (if (has-errors? agg)
       (reduced agg)
       (with agg (validator agg))))
   agg
   (validators agg)))

(defn required-validator [agg->entities attrs]
  (fn [agg]
    (let [entities (agg->entities agg)
          data     (q agg
                      '{:find  [?e ?a]
                        :in    [[?e ...] [?a ...]]
                        :where [[(missing? $ ?e ?a)]]}
                      entities attrs)]
      (for [[e a] data]
        {:error/type   :required
         :error/entity e
         :error/attr   a}))))

(defn predicate-validator [agg->entities pred-map]
  (fn [agg]
    (let [entities (agg->entities agg)
          data     (q agg
                      '{:find  [?e ?a ?v ?pred]
                        :in    [?apply [?e ...] [[?a ?pred]]]
                        :where [[?e ?a ?v]
                                (not [(?apply ?pred ?v [])])]}
                      apply entities pred-map)]
      (for [[e a v pred] data]
        {:error/type   :predicate
         :error/entity e
         :error/attr   a
         :error/value  v
         :error/pred   pred}))))

(defn query-validator [agg->entities entity->value predicate]
  (fn [agg]
    (let [entities (agg->entities agg)]
      (for [e    entities
            :let [v (entity->value agg e)]]
        (if (not (predicate v))
          {:error/type      :query
           :error/entity    e
           :error/value     v
           :error/predicate predicate})))))
