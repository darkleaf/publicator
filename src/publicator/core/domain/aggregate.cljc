(ns publicator.core.domain.aggregate
  (:require
   [datascript.core :as d]
   [medley.core :as m]))

(defonce schema-of-aggregate (atom {:error/entity {:db/valueType :db.type/ref}}))

(defonce schema-of-validators (atom {:validator/attribute {:db/index true}
                                     #_#_:validator/type  {:db/index true}}))


(defn has-errors? [agg]
  (-> agg (d/datoms :aevt :error/entity) (seq) (boolean)))

(defn has-no-errors? [agg]
  (not (has-errors? agg)))

(defmulti errors-tx (fn [agg validator] (:validator/type validator)))

(defn validate [agg validators]
  (->> (for [validator (d/q '[:find [(pull ?e [*]) ...]
                              :where [?e :validator/type _]]
                            validators)]
         [:db.fn/call errors-tx validator])
       (d/db-with agg)))

(defn validate! [agg validators]
  (let [agg (validate agg validators)]
    (if (has-errors? agg)
      (throw (ex-info "Invalid aggregate" {:agg agg :validators validators}))
      agg)))

(defn remove-errors [agg]
  (->> (for [id (d/q '[:find [?e ...]
                       :where [?e :error/entity _]]
                     agg)]
         [:db.fn/retractEntity id])
       (d/db-with agg)))


(defn retract-validators-by-attribute [validators attribute]
  (->> (for [id (d/q '[:find [?e ...]
                       :in $ ?attribute
                       :where [?e :validator/attribute ?attribute]]
                     validators attribute)]
         [:db.fn/retractEntity id])
       (d/db-with validators)))


(swap! schema-of-validators assoc
       :predicate/ident {:db/unique :db.unique/identity})

(defn upsert-predicate-validator [agg attribute predicate]
  (->> [{:validator/type      :predicate
         :validator/attribute attribute
         :predicate/ident     attribute
         :predicate/test      predicate}]
       (d/db-with agg)))

;; todo: protocol
(defn- apply-predicate
  [p x]
  (cond
    (nil? p)      true
    (vector? p)   (some #{x} p)
    (set? p)      (p x)
    (fn? p)       (p x)
    (m/regexp? p) (and (string? x) (re-matches p x))))

;; Регулярки и функции несравнимы.
;; Поэтому предикат не упоминается в ошибке, но его можно получить из валидаторов.
(defmethod errors-tx :predicate [agg {:keys [validator/attribute predicate/test]}]
  (for [[e a v] (d/datoms agg :aevt attribute)
        :when   (not (apply-predicate test v))]
    {:error/type      :predicate
     :error/entity    e
     :error/attribute a
     :error/value     v}))


(swap! schema-of-validators assoc
       :required/ident {:db/unique :db.unique/identity})

(defn upsert-required-validator [agg attribute rule]
  (->> [{:validator/type      :required
         :validator/attribute attribute
         :required/ident      attribute
         :required/rule       rule}]
       (d/db-with agg)))

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


(def root-entity-rule
  '[[(entity ?e)
     [?e :db/ident :root]]])

(defn root [agg]
  (d/entity agg :root))

(defn id [agg]
  (d/q '[:find ?id .
         :where [:root :agg/id ?id]]
       agg))

(defn new-aggregate []
  (-> (d/empty-db @schema-of-aggregate)
      (d/db-with [[:db/add 1 :db/ident :root]])))

(defn new-validators []
  (-> (d/empty-db @schema-of-validators)
      (upsert-predicate-validator :agg/id int?)))
