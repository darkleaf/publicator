(ns publicator.domain.aggregates.admin
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.utils.datascript.validation :as d.validation]))

(def ^:const states #{:active :archived})

(def spec
  {:type        :admin
   :defaults-tx (fn [] [[:db/add :root :admin/state :active]])
   :validator   (d.validation/compose
                 (d.validation/attributes [:admin/state states])
                 (d.validation/in-case-of agg/root-q
                                          [:admin/state some?]))})
