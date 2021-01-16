(ns publicator.core.domain.aggregate
  (:require
   [datascript.core :as d]
   [medley.core :as m]
   [publicator.utils :as u]))

;; utils

(defn with-schema [db new-schema]
  (-> (d/empty-db new-schema)
      (with-meta (meta db))
      (d/db-with (d/datoms db :eavt))))

(defn vary-schema [db f & args]
  (with-schema db (apply f (d/schema db) args)))

;; filtered db is read-only
;; наверное отсюда нужно вытащить db, а то больно сложная логика у фукнции
(defn filter-datoms [agg allowed-attr?]
  (let [pred   (comp (some-fn #(= "db" (namespace %))
                              allowed-attr?)
                     :a)
        datoms (->> (d/datoms agg :eavt)
                    (filter pred))
        schema (d/schema agg)]
    (d/init-db datoms schema)))

;; errors

(defn has-errors? [agg]
  (-> agg (d/datoms :aevt :error/entity) (seq) (boolean)))

(defn has-no-errors? [agg]
  (not (has-errors? agg)))

(defmulti errors-tx (fn [agg validator] (:validator/type validator)))

(defn validate [agg validators]
  (let [tx-data (for [validator (d/q '[:find [(pull ?e [*]) ...]
                                       :where [?e :validator/type _]]
                                     validators)]
                  [:db.fn/call errors-tx validator])]
    (d/db-with agg tx-data)))

(defn validate! [agg validators]
  (let [agg (validate agg validators)]
    (if (has-errors? agg)
      (throw (ex-info "Invalid aggregate" {:agg agg :validators validators}))
      agg)))

(defn remove-errors [agg]
  (let [tx-data (for [id (d/q '[:find [?e ...]
                                :where [?e :error/entity _]]
                              agg)]
                  [:db.fn/retractEntity id])]
    (d/db-with agg tx-data)))

(defn- retract-by-attribute-tx [validators attribute]
  (for [id (d/q '[:find [?e ...]
                  :in $ ?attribute
                  :where [?e :validator/attribute ?attribute]]
                validators attribute)]
    [:db.fn/retractEntity id]))

(defn- retract-mixin [validators]
  (-> validators
      (d/db-with [{:db/ident :retract/by-attribute
                   :db/fn retract-by-attribute-tx}])))

;; predicate validator

(defn- predicate-upsert-tx [_agg attribute predicate]
  [{:validator/type      :predicate
    :validator/attribute attribute
    :predicate/ident     attribute
    :predicate/test      predicate}])

(defn- predicate-validator-mixin [validators]
  (-> validators
      (vary-schema assoc :predicate/ident {:db/unique :db.unique/identity})
      (d/db-with [{:db/ident :predicate/upsert
                   :db/fn    predicate-upsert-tx}
                  #_{:db/ident :predicate/retract}])))

;; todo: protocol
(defn- apply-predicate
  [p x]
  (cond
    (nil? p)      true
    (vector? p)   (some #{x} p)
    (set? p)      (p x)
    (fn? p)       (p x)
    (m/regexp? p) (re-matches p x)))

;; Регулярки и функции несравнимы.
;; Поэтому предикат не упоминается в ошибке, но его можно получить из валидаторов.
(defmethod errors-tx :predicate [agg {:keys [validator/attribute predicate/test]}]
  (for [[e a v] (d/datoms agg :aevt attribute)
        :when   (not (apply-predicate test v))]
    {:error/type      :predicate
     :error/entity    e
     :error/attribute a
     :error/value     v}))

;; required validator

(defn- required-upsert-tx [_agg attribute rule]
  [{:validator/type      :required
    :validator/attribute attribute
    :required/ident      attribute
    :required/rule       rule}])

(defn- required-validator-mixin [validators]
  (-> validators
      (vary-schema assoc :required/ident {:db/unique :db.unique/identity})
      (d/db-with [{:db/ident :required/upsert
                   :db/fn    required-upsert-tx}])))

(defmethod errors-tx :required [agg {:keys [validator/attribute required/rule]}]
  (for [e (d/q '[:find [?e ...]
                 :in $ % ?a
                 :where
                 (entity ?e)
                 [(missing? $ ?e ?a)]]
               agg rule attribute)]
    {:error/type      :required
     :error/entity    e
     :error/attribute attribute}))

;; main

(def root-entity-rule
  '[[(entity ?e)
     [?e :db/ident :root]]])

(defn root [agg]
  (d/entity agg :root))

(defn id [agg]
  (d/q '[:find ?id .
         :where [:root :agg/id ?id]]
       agg))


;; тут идея в том, что кому нужно могут и функцию сделать, например поставить текущую дату
;; или вообще это будет генератор*

(def proto-agg
  (-> (d/empty-db {:error/entity {:db/valueType :db.type/ref}})
      (d/db-with [[:db/add 1 :db/ident :root]])))

(def proto-validators
  (-> (d/empty-db {:attribute       {:db/index true}})
      (predicate-validator-mixin)
      (required-validator-mixin)
      (retract-mixin)
      (d/db-with [[:predicate/upsert :agg/id int?]])))
