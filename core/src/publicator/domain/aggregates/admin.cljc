(ns publicator.domain.aggregates.admin
  (:require
   [publicator.domain.aggregate :as agg]))

(def ^:const states #{:active :archived})

(defn- validate-d [super agg]
  (-> (super agg)
      (agg/predicate-validator 'root  {:admin/state states})
      (agg/required-validator  'root #{:admin/state})))

(def blank (-> agg/blank
               (vary-meta assoc :type :agg/admin)
               (agg/decorate {`agg/validate #'validate-d})))
