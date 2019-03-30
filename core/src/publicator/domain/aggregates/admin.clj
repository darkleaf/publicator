(ns publicator.domain.aggregates.admin
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.utils.datascript.validation :as d.validation]))

(def ^:const states #{:active :archived})

(def spec
  {:type      :admin
   :validator (d.validation/compose
               (d.validation/predicate [[:admin/state states]])
               (d.validation/required agg/root-q #{:admin/state}))})
