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

(defn- check-required [errors report ids attrs]
  (let [errors (d/q '{:find  [?e ?a]
                      :in    [$after $errors [?e ...] [?a ...]]
                      :where [($errors not-join [?e ?a]
                                       [?err :entity ?e]
                                       [?err :attribute ?a]
                                       [?err :type ::required])
                              [(missing? $after ?e ?a)]]}
                    (:db-after report) errors ids attrs)]
    (for [error errors]
      (-> (zipmap [:entity :attribute]
                  error)
          (assoc :type ::required)))))

(defn required
  ([attrs] (required all-q attrs))
  ([entities-q attrs]
   (fn [report]
     (let [ids (d/q entities-q (:db-after report))]
       [[:db.fn/call check-required report ids attrs]]))))

(defn query [entities-q value-q pred & args]
   (fn [report]
     (let [args      (vec args)
           db-after  (:db-after report)
           ids       (d/q entities-q db-after)
           id-value  (for [id ids]
                       [id (d/q value-q db-after id)])
           with-errs (remove (fn [[id value]] (apply pred value args))
                             id-value)]
       (for [[id value] with-errs]
         {:entity    id
          :value-q   value-q
          :value     value
          :predicate pred
          :args      args
          :type      ::query}))))
