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

(defn- without-attribute-errors [chain ids attribute]
  (d/q '{:find  [[?e ...]]
         :in    [$ [?e ...] ?a]
         :where [(not-join [?e ?a]
                   [?err :entity ?e]
                   [?err :attribute ?a])]}
       (:errors chain) ids attribute))

(defn- apply-check [chain ids [attr pred & args]]
  (let [args   (vec args)
        ids    (without-attribute-errors chain ids attr)
        agg    (:aggregate chain)
        errors (d/q '{:find  [?e ?a ?v ?pred ?args]
                      :in    [$ [?e ...] ?a ?pred ?args]
                      :where [[?e ?a ?v]
                              (not [(clojure.core/apply ?pred ?v ?args)])]}
                    agg ids attr pred args)
        tx-data (for [error errors]
                  (-> (zipmap [:entity :attribute :value :predicate :args]
                              error)
                      (assoc :type ::predicate)))]
    (update chain :errors d/db-with tx-data)))

(defn- check-required [chain ids [attr & _]]
  (let [ids    (without-attribute-errors chain ids attr)
        agg    (:aggregate chain)
        errors (d/q '{:find  [?e ?a]
                      :in    [$ [?e ...] ?a]
                      :where [[(missing? $ ?e ?a)]]}
                    agg ids attr)
        tx-data (for [error errors]
                  (-> (zipmap [:entity :attribute]
                              error)
                      (assoc :type ::required)))]
    (update chain :errors d/db-with tx-data)))

(defn types [chain & checks]
  (let [agg (:aggregate chain)
        ids (d/q '[:find [?e ...] :where [?e _ _]] agg)]
    (reduce (fn [chain check] (apply-check chain ids check))
            chain checks)))

(defn required-for [chain entities-q & checks]
  (let [agg (:aggregate chain)
        ids (d/q entities-q agg)]
    (reduce (fn [chain check]
              (-> chain
                  (check-required ids check)
                  (apply-check ids check)))
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
