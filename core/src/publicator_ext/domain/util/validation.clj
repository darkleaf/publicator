(ns publicator-ext.domain.util.validation
  (:require
   [datascript.core :as d]))

(defn begin [aggregate]
  {:aggregate aggregate
   :errors    (d/empty-db)})

(defn end [chain]
  (:errors chain))

(defn aggregate [chain]
  (:aggregate chain))

(defn- attribute-normalize-check [[kind attribute predicate & args]]
  [kind attribute predicate (vec args)])

(defn- attribute-required [chain ids check]
  (let [agg      (:aggregate chain)
        errors   (d/q '{:find  [?e ?a]
                        :in    [$ [?e ...] [?kind ?a _ _]]
                        :where [[(= ?kind :req)]
                                [(missing? $ ?e ?a)]]}
                      agg ids check)
        tx-data  (for [error errors]
                   (-> (zipmap [:entity :attribute] error)
                       (assoc :type ::required)))]
    (update chain :errors d/db-with tx-data)))

(defn- attribute-predicate [chain ids check]
  (let [agg      (:aggregate chain)
        errors   (d/q '{:find  [?e ?a ?v ?pred ?args]
                        :in    [$ [?e ...] [_ ?a ?pred ?args]]
                        :where [[?e ?a ?v]
                                (not [(clojure.core/apply ?pred ?v ?args)])]}
                      agg ids check)
        tx-data  (for [error errors]
                   (-> (zipmap [:entity :attribute :value :predicate :args] error)
                       (assoc :type ::predicate)))]
    (update chain :errors d/db-with tx-data)))

(defn- attribute [chain ids check]
  (let [errors (:errors chain)
        ids    (d/q '{:find  [[?e ...]]
                      :in    [$ [?e ...] [_ ?a _ _]]
                      :where [(not-join [?e ?a]
                                [?err :entity ?e]
                                [?err :attribute ?a])]}
                    errors ids check)]
    (-> chain
        (attribute-required  ids check)
        (attribute-predicate ids check))))

(defn attributes [chain entities-q checks]
  (let [checks (map attribute-normalize-check checks)
        agg    (:aggregate chain)
        errs   (:errors chain)
        ids    (d/q entities-q agg)]
    (reduce (fn [chain check] (attribute chain ids check))
            chain checks)))

(defn query [chain entities-q query pred & args]
  (let [args        (vec args)
        agg         (:aggregate chain)
        ids         (d/q entities-q agg)
        value-by-id (for [id ids]
                      [id (d/q query agg id)])
        with-errs   (remove (fn [[id value]] (apply pred value args))
                            value-by-id)
        tx-data     (for [[id value] with-errs]
                      {:entity    id
                       :value     value
                       :query     query
                       :predicate pred
                       :args      args
                       :type      ::query})]
    (update chain :errors d/db-with tx-data)))
