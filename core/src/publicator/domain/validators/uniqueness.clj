(ns publicator.domain.validators.uniqueness
  (:require
   [publicator.domain.abstractions.uniqueness :refer [*is-unique*]]
   [publicator.domain.aggregate :as agg]))

(defn validator [root-attrs]
  (fn [agg]
    (let [root       (agg/root agg)
          id         (:root/id root)
          root-attrs (sort root-attrs)
          root-vals  (map #(get root %)
                          root-attrs)]
      (when-not (*is-unique* id root-attrs root-vals)
        [{:type       ::attributes-not-unique
          :aggregate  agg
          :attributes root-attrs
          :values     root-vals}]))))
