(ns publicator.utils.datascript.validation
  (:require
   [datascript.core :as d]))

(defn compose [& validators]
  (fn [report]
    (reduce (fn [acc validator]
              (into acc (validator report)))
            []
            validators)))

(defn validate [report validator]
  (let [errors  (d/empty-db)
        tx-data (validator report)]
    (d/db-with errors tx-data)))

(defn- without-attribute-errors [errors ids attribute]
  (d/q '{:find  [[?e ...]]
         :in    [$ [?e ...] ?a]
         :where [(not-join [?e ?a]
                   [?err :entity ?e]
                   [?err :attribute ?a])]}
       errors ids attribute))

(defn- check-attribute [errors db ids [attr pred & args]]
  (let [args   (vec args)
        ids    (without-attribute-errors errors ids attr)
        errors (d/q '{:find  [?e ?a ?v ?pred ?args]
                      :in    [$ [?e ...] ?a ?pred ?args]
                      :where [[?e ?a ?v]
                              (not [(clojure.core/apply ?pred ?v ?args)])]}
                    db ids attr pred args)]
    (for [error errors]
      (-> (zipmap [:entity :attribute :value :predicate :args]
                  error)
          (assoc :type ::predicate)))))

(defn attributes [& checks]
  (fn [report]
    (let [db  (:db-after report)
          ids (d/q '{:find  [[?e ...]]
                     :where [[?e _ _]]}
                   db)]
      (for [check checks]
        [:db.fn/call check-attribute db ids check]))))

(defn- check-required [errors db ids [attr & _]]
  (let [ids     (without-attribute-errors db ids attr)
        errors  (d/q '{:find  [?e ?a]
                       :in    [$ [?e ...] ?a]
                       :where [[(missing? $ ?e ?a)]]}
                     db ids attr)]
    (for [error errors]
      (-> (zipmap [:entity :attribute]
                  error)
          (assoc :type ::required)))))

(defn in-case-of [entities-q & checks]
  (fn [report]
    (let [db  (:db-after report)
          ids (d/q entities-q db)]
      (for [check checks]
        [:db.fn/call (fn [errors]
                       (concat (check-required errors db ids check)
                               (check-attribute errors db ids check)))]))))

(defn query-resp [entities-q query pred & args]
  (fn [report]
    (let [db          (:db-after report)
          args        (vec args)
          ids         (d/q entities-q db)
          value-by-id (for [id ids]
                        [id (d/q query db id)])
          with-errs   (remove (fn [[id value]] (apply pred value args))
                              value-by-id)]
      (for [[id value] with-errs]
        {:entity    id
         :value     value
         :query     query
         :predicate pred
         :args      args
         :type      ::query}))))
