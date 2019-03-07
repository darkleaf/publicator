(ns publicator.domain.aggregate.validation
  (:require
   [datascript.core :as d]))

(defn- without-attribute-errors [agg ids attribute]
  (d/q '{:find  [[?e ...]]
         :in    [$ [?e ...] ?a]
         :where [(not-join [?e ?a]
                   [?err :entity ?e]
                   [?err :attribute ?a])]}
      (-> agg meta :aggregate/errors) ids attribute))

(defn- check-attribute [agg ids [attr pred & args]]
  (let [args    (vec args)
        ids     (without-attribute-errors agg ids attr)
        errors  (d/q '{:find  [?e ?a ?v ?pred ?args]
                       :in    [$ [?e ...] ?a ?pred ?args]
                       :where [[?e ?a ?v]
                               (not [(clojure.core/apply ?pred ?v ?args)])]}
                     agg ids attr pred args)
        tx-data (for [error errors]
                  (-> (zipmap [:entity :attribute :value :predicate :args]
                              error)
                      (assoc :type ::predicate)))]
    (vary-meta agg update :aggregate/errors d/db-with tx-data)))

(defn- check-required [agg ids [attr & _]]
  (let [ids     (without-attribute-errors agg ids attr)
        errors  (d/q '{:find  [?e ?a]
                       :in    [$ [?e ...] ?a]
                       :where [[(missing? $ ?e ?a)]]}
                     agg ids attr)
        tx-data (for [error errors]
                  (-> (zipmap [:entity :attribute]
                              error)
                      (assoc :type ::required)))]
    (vary-meta agg update :aggregate/errors d/db-with tx-data)))

(defn attributes [agg & checks]
  (let [ids (d/q '{:find  [[?e ...]]
                   :where [[?e _ _]]}
                 agg)]
    (reduce (fn [agg check] (check-attribute agg ids check))
            agg checks)))

(defn in-case-of [agg entities-q & checks]
  (let [ids (d/q entities-q agg)]
    (reduce (fn [agg check]
              (-> agg
                  (check-required ids check)
                  (check-attribute ids check)))
            agg checks)))

(defn query-resp [agg entities-q query pred & args]
  (let [args        (vec args)
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
    (vary-meta agg update :aggregate/errors d/db-with tx-data)))
