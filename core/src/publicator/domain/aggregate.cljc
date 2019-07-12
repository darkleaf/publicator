(ns publicator.domain.aggregate
  (:require
   [datascript.core :as d]
   [datascript.parser :as d.p]
   [clojure.core.match :as m]))

(defprotocol Aggregate
  :extend-via-metadata true
  (rules [agg])
  (validate [agg])
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

(defn- validate-impl [agg]
  agg)

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
        {`rules    #'rules-impl
         `validate #'validate-impl
         `msg->tx  #'msg->tx-impl})))

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

(defn required-validator [agg agg->entities attrs]
  (let [entities (agg->entities agg)
        data     (q agg
                    '{:find  [?e ?a]
                      :in    [[?e ...] [?a ...]]
                      :where [[(missing? $ ?e ?a)]]}
                    entities attrs)
        tx-data (for [[e a] data]
                  {:error/type   :required
                   :error/entity e
                   :error/attr   a})]
    (with agg tx-data)))

(defn predicate-validator [agg agg->entities pred-map]
  (if (has-errors? agg)
    agg
    (let [entities (agg->entities agg)
          data     (q agg
                      '{:find  [?e ?a ?v ?pred]
                        :in    [?apply [?e ...] [[?a ?pred]]]
                        :where [[?e ?a ?v]
                                (not [(?apply ?pred ?v [])])]}
                      apply entities pred-map)
          tx-data  (for [[e a v pred] data]
                     {:error/type   :predicate
                      :error/entity e
                      :error/attr   a
                      :error/value  v
                      :error/pred   pred})]
      (with agg tx-data))))

(defn query-validator [agg agg->entities entity->result predicate]
  (if (has-errors? agg)
    agg
    (let [entities (agg->entities agg)
          tx-data  (for [e    entities
                         :let [res (entity->result agg e)]]
                     (if (not (predicate res))
                       {:error/type      :query
                        :error/entity    e
                        :error/result    res
                        :error/predicate predicate}))]
      (with agg tx-data))))
