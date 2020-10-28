(ns publicator.core.use-cases.services.form
  (:require
   [publicator.core.domain.aggregate :as agg]
   [darkleaf.effect.core :refer [effect]]
   [datascript.core :as d]
   [clojure.data :as data])
  #?(:cljs (:require-macros [publicator.core.use-cases.services.form :refer [check-errors*]])))

(defn changes [agg form updatable-attr?]
  (let [pred                       (comp updatable-attr? :a)
        [agg-datoms form-datoms _] (data/diff agg form)
        del                        (->> agg-datoms
                                        (filter pred)
                                        (map #(assoc % :added false)))
        add                        form-datoms
        changes                    (concat del add)
        rejected                   (->> changes
                                        (remove pred)
                                        (not-empty))]
    (if (some? rejected)
      (throw (ex-info "Rejected datoms" {:rejected rejected})))
    changes))

(defn -check-errors* [form ns-name]
  (let [tag (keyword ns-name "->invalid-form")]
    (if (agg/has-errors? form)
      (effect tag form)
      form)))

(defmacro check-errors* [form]
  (let [ns (-> *ns* ns-name str)]
    `(-check-errors* ~form ~ns)))
