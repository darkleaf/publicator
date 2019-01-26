(ns publicator-ext.domain.util.validation
  (:require
   [datascript.core :as d]
   [backtick :as bt]))

(defn begin [aggregate]
  {:aggregate aggregate
   :errors    (d/empty-db)})

(defn complete [chain]
  (:errors chain))

(defn aggregate [chain]
  (:aggregate chain))

(defn- attribute-normalize-check [[kind attribute predicate & args]]
  [kind attribute predicate (vec args)])

(defn- attribute-required [chain rules checks]
  (let [agg     (:aggregate chain)
        errors  (d/q '{:find  [?e ?a]
                       :in    [$ % [[?kind ?a _ _]]]
                       :where [(entity ?e)
                               [(= ?kind :req)]
                               [(missing? $ ?e ?a)]]}
                     agg rules checks)
        tx-data (for [error errors]
                  (-> (zipmap [:entity :attribute] error)
                      (assoc :type ::required)))]
    (update chain :errors d/db-with tx-data)))

(defn- attribute-predicate [chain rules checks]
  (let [agg     (:aggregate chain)
        errors  (d/q '{:find  [?e ?a ?v ?pred ?args]
                       :in    [$ % [[_ ?a ?pred ?args]]]
                       :where [(entity ?e)
                               [?e ?a ?v]
                               (not [(clojure.core/apply ?pred ?v ?args)])]}
                     agg rules checks)
        tx-data (for [error errors]
                  (-> (zipmap [:entity :attribute :value :predicate :args] error)
                      (assoc :type ::predicate)))]
    (update chain :errors d/db-with tx-data)))

;; TODO: spec
(defn attributes [chain rules checks]
  (let [checks (map attribute-normalize-check checks)]
    (-> chain
        (attribute-required  rules checks)
        (attribute-predicate rules checks))))
