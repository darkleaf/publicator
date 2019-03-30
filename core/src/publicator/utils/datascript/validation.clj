(ns publicator.utils.datascript.validation
  (:require
   [datascript.core :as d]))

(def null-validator (fn [db] []))

(defn compose [& validators]
  (fn [db]
    (reduce (fn [acc validator]
              (into acc (validator db)))
            []
            validators)))

(defn validate [db validator]
  (let [errors  (d/empty-db)
        tx-data (validator db)]
    (d/db-with errors tx-data)))

(def all-q
  '{:find  [[?e ...]]
    :where [[?e _ _]]})

(defn- check-predicate [errors db ids [attr pred & args]]
  (let [args   (vec args)
        errors (d/q '{:find  [?e ?a ?v ?pred ?args]
                      :in    [$ $errors [?e ...] ?a ?pred ?args]
                      :where [($errors not-join [?e ?a]
                                       [?err :entity ?e]
                                       [?err :attribute ?a]
                                       [?err :type ::predicate])
                              [?e ?a ?v]
                              (not [(clojure.core/apply ?pred ?v ?args)])]}
                    db errors ids attr pred args)]
    (for [error errors]
      (-> (zipmap [:entity :attribute :value :predicate :args]
                  error)
          (assoc :type ::predicate)))))

(defn predicate
  ([checks] (predicate all-q checks))
  ([entities-q checks]
   (fn [db]
     (let [ids (d/q entities-q db)]
       (for [check checks]
         [:db.fn/call check-predicate db ids check])))))

(defn- check-required [errors db ids attrs]
  (let [errors (d/q '{:find  [?e ?a]
                      :in    [$ $errors [?e ...] [?a ...]]
                      :where [($errors not-join [?e ?a]
                                       [?err :entity ?e]
                                       [?err :attribute ?a]
                                       [?err :type ::required])
                              [(missing? $ ?e ?a)]]}
                    db errors ids attrs)]
    (for [error errors]
      (-> (zipmap [:entity :attribute]
                  error)
          (assoc :type ::required)))))

(defn required
  ([attrs] (required all-q attrs))
  ([entities-q attrs]
   (fn [db]
     (let [ids (d/q entities-q db)]
       [[:db.fn/call check-required db ids attrs]]))))

(defn query [entities-q value-q pred & args]
   (fn [db]
     (let [args      (vec args)
           ids       (d/q entities-q db)
           id-value  (for [id ids]
                       [id (d/q value-q db id)])
           with-errs (remove (fn [[id value]] (apply pred value args))
                             id-value)]
       (for [[id value] with-errs]
         {:entity    id
          :value-q   value-q
          :value     value
          :predicate pred
          :args      args
          :type      ::query}))))
