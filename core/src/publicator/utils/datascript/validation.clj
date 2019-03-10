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

(def all-q
  '{:find  [[?e ...]]
    :where [[?e _ _]]})

(defn- check-predicate [errors report ids [attr pred & args]]
  (let [args   (vec args)
        errors (d/q '{:find  [?e ?a ?v ?pred ?args]
                      :in    [$before $after $errors [?e ...] ?a ?pred ?args]
                      :where [($errors not-join [?e ?a]
                                       [?err :entity ?e]
                                       [?err :attribute ?a]
                                       [?err :type ::predicate])
                              [$after ?e ?a ?v]
                              (not [$before ?e ?a ?v])
                              (not [(clojure.core/apply ?pred ?v ?args)])]}
                    (:db-before report) (:db-after report) errors ids attr pred args)]
    (for [error errors]
      (-> (zipmap [:entity :attribute :value :predicate :args]
                  error)
          (assoc :type ::predicate)))))

(defn predicate
  ([checks] (predicate all-q checks))
  ([entities-q checks]
   (fn [report]
     (let [ids (d/q entities-q (:db-after report))]
       (for [check checks]
         [:db.fn/call check-predicate report ids check])))))

(defn- check-required [errors db ids [attr & _]]
  (let [errors  (d/q '{:find  [?e ?a]
                       :in    [$ $errors [?e ...] ?a]
                       :where [($errors not-join [?e ?a]
                                        [?err :entity ?e]
                                        [?err :attribute ?a]
                                        [?err :type ::required])
                               [(missing? $ ?e ?a)]]}
                     db errors ids attr)]
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
