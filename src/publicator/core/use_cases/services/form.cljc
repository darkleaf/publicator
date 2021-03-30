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


#_"

я бы хотел все-таки отслеживать удаление сущностей.

есть агрегат
есть предикат (attr -> true), показывающий,можно ли читать атрибут в форме.
из агрегата с помощью предиката сделали форму

форму как-то отредактировали
отдали обратно

нужно понять, что поменяли и можно ли было это менять
берем агрегат и отредактированную фому,
смотрим разницу

видимо нужно смотреть разницу между формами и герерировать в том чиле :db/retractEntity

и еще всякие приколы с тем, что агрегат ведь не залочен, его могли отредактировать.


Я бы наверное хотел видеть вообще список вроде:

[:deleted {:db/id 42, :attr 42, ...}]
[:added   {:db/id 43, :attr 43, ...}]
[:changed {:db/id 44

Ну например, и там и там создали сущность №42 и что делать?

или вообще diff применить к каждой сущности индивидуально?


"
