(ns publicator-ext.domain.errors
  (:require
   [datascript.core :as d]
   [backtick :as bt]))

(defn build [aggregate]
  {:aggregate aggregate
   :errors    (d/empty-db)})

(defn extract [this] (:errors this))

(defn attributes [this conditions checks]
  (let [agg     (:aggregate this)
        query   (bt/template {:find  [?e ?attr# ?reason#]
                              :in    [$ [[?attr# ?pred# ?reason#]]]
                              :where [~@conditions
                                      (not-join [?e ?attr# ?pred#]
                                        [?e ?attr# ?val#]
                                        ;; https://github.com/tonsky/datascript/issues/283
                                        [(clojure.core/apply ?pred# ?val# [])])]})
        errors  (d/q query agg checks)
        tx-data (for [[e attr reason] errors]
                  {:error/entity e
                   :error/attr   attr
                   :error/reason reason})]
     (update this :errors d/db-with tx-data)))
