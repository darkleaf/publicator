(ns publicator.core.domain.aggregates.translation
  (:require [publicator.core.domain.aggregate :as agg]
            [datascript.core :as d]))

; вообще, хочется тут иметь `(ordered-set :en :ru)`,
; чтобы был порядок как при объявлении, но нет версии под cljs
; https://github.com/clj-commons/ordered/issues/43

(def langs #{:en :ru})
(def default-lang :en)

;; перевод у агрегата может быть только один
;; у перевода могут быть и зависимые сущности с переводом, вроде элементов заказа,
;; при этом для них не нужно указывать `:translation/lang`
;; т.е. `:translation/lang` может быть только у сущностей второго уровня

(swap! agg/schema merge
       {:translation/root {:db/valueType :db.type/ref}
        :translation/lang {:db/unique     :db.unique/identity
                           :agg/predicate langs}})

(defn validate [agg]
  (agg/required-attrs-validator agg {:translation/_root [:translation/lang]}))

(defn full-translation-validator [agg]
  (let [actual-langs (->> agg
                          (d/q '[:find [?lang ...]
                                 :where
                                 [?e :translation/root :root]
                                 [?e :translation/lang ?lang]])
                          (set))]
    (if (= langs actual-langs)
      agg
      (d/db-with agg [{:error/type           :full-translation
                       :error/entity         :root
                       :error/actual-langs   actual-langs
                       :error/expected-langs langs}]))))
