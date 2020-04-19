(ns publicator.core.use-cases.services.form
  (:require
   [publicator.core.domain.aggregate :as agg]
   [darkleaf.effect.core :refer [effect]]
   [datascript.core :as d]
   [clojure.data :as data]))

(defn agg->form [agg readable-attr?]
  (let [datoms (->> (d/datoms agg :eavt)
                    (filter (comp readable-attr? :a)))
        schema (:schema agg)]
    (d/init-db datoms schema)))

(defn apply-form! [agg form updatable-attr?]
  (let [[agg-datoms form-datoms _] (data/diff agg form)
        del                        (->> agg-datoms
                                        (filter (comp updatable-attr? :a))
                                        (map #(assoc % :added false)))
        add                        form-datoms
        changes                    (concat del add)
        rejected                   (->> changes
                                        (remove (comp updatable-attr? :a))
                                        (not-empty))]
    (if (some? rejected)
      (throw (ex-info "Rejected datoms" {:rejected rejected}))
      (d/db-with agg changes))))

(defn check-errors* [form ns-name]
  (let [key (keyword ns-name "->invalid-form")]
    (if (agg/has-errors? form)
      (effect [key form])
      form)))

(defmacro check-errors [form]
  (let [ns (-> *ns* ns-name str)]
    `(check-errors* ~form ~ns)))
