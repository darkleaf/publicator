(ns publicator.core.domain.aggregates.translation
  (:require [publicator.core.domain.aggregate :as agg]
            [datascript.core :as d]))

(def langs [:en :ru])
(def default-lang (first langs))

;; перевод у агрегата может быть только один
;; у перевода могут быть и зависимые сущности с переводом, вроде элементов заказа,
;; при этом для них не нужно указывать `:translation/lang`
;; т.е. `:translation/lang` может быть только у сущностей второго уровня

(def schema
  {:translation/root {:db/valueType :db.type/ref}
   :translation/lang {:db/unique     :db.unique/identity
                      :agg/predicate langs}})

(defn- full-translation-validator [agg]
  (let [missed-langs (d/q '[:find [?expected ...]
                            :in $ [?expected ...]
                            :where
                            (not
                             [?e :translation/root :root]
                             [?e :translation/lang ?lang]
                             [(= ?lang ?expected)])]
                          agg langs)]
    (if (empty? missed-langs)
      agg
      (d/db-with agg [{:error/type         :full-translation
                       :error/entity       :root
                       :error/missed-langs missed-langs}]))))

(defn validate [agg & {:keys [full-translation]}]
  (cond-> agg
    true             (agg/required-attrs-validator {:translation/_root [:translation/lang]})
    full-translation (full-translation-validator)))
