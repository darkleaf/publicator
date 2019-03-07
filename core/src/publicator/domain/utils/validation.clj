(ns publicator.domain.utils.validation
  (:require
   [datascript.core :as d]))

;; может быть это вынести
;; или пропатчить изначальный неймспейс
(def ^{:private true, :arglists '([db tx-data])}
  db-with
  (fnil d/db-with (d/empty-db)))

(def ^{:private true, :arglists '([query & inputs])}
  q
  (fnil d/q (d/empty-db)))


(defn- without-attribute-errors [db ids attribute]
  (q '{:find  [[?e ...]]
       :in    [$ [?e ...] ?a]
       :where [(not-join [?e ?a]
                 [?err :entity ?e]
                 [?err :attribute ?a])]}
     (-> db meta :aggregate/errors) ids attribute))

(defn- check-attribute [db ids [attr pred & args]]
  (let [args   (vec args)
        ids    (without-attribute-errors db ids attr)
        errors (q '{:find  [?e ?a ?v ?pred ?args]
                    :in    [$ [?e ...] ?a ?pred ?args]
                    :where [[?e ?a ?v]
                            (not [(clojure.core/apply ?pred ?v ?args)])]}
                  db ids attr pred args)
        tx-data (for [error errors]
                  (-> (zipmap [:entity :attribute :value :predicate :args]
                              error)
                      (assoc :type ::predicate)))]
    (vary-meta db update :aggregate/errors db-with tx-data)))

(defn- check-required [db ids [attr & _]]
  (let [ids    (without-attribute-errors db ids attr)
        errors (d/q '{:find  [?e ?a]
                      :in    [$ [?e ...] ?a]
                      :where [[(missing? $ ?e ?a)]]}
                    db ids attr)
        tx-data (for [error errors]
                  (-> (zipmap [:entity :attribute]
                              error)
                      (assoc :type ::required)))]
    (vary-meta db update :aggregate/errors db-with tx-data)))

(defn types [db & checks]
  (let [ids (d/q '{:find  [[?e ...]]
                   :where [[?e _ _]]}
                 db)]
    (reduce (fn [db check] (check-attribute db ids check))
            db checks)))

(defn required-for [db entities-q & checks]
  (let [ids (d/q entities-q db)]
    (reduce (fn [db check]
              (-> db
                  (check-required ids check)
                  (check-attribute ids check)))
            db checks)))

(defn query [db entities-q query pred & args]
  (let [args        (vec args)
        ids         (d/q entities-q db)
        value-by-id (for [id ids]
                      [id (d/q query db id)])
        with-errs   (remove (fn [[id value]] (apply pred value args))
                            value-by-id)
        tx-data     (for [[id value] with-errs]
                      {:entity    id
                       :value     value
                       :query     query
                       :predicate pred
                       :args      args
                       :type      ::query})]
    (vary-meta db update :aggregate/errors db-with tx-data)))




;; attributes
;; in-case-of
;; query-resp
