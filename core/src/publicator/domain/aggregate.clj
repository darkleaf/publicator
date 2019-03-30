(ns publicator.domain.aggregate
  (:refer-clojure :exclude [type])
  (:require
   [publicator.domain.abstractions.instant :as instant]
   [publicator.utils.datascript.validation :as d.validation]
   [datascript.core :as d]
   [clojure.set :as set]))

(defn root [agg]
  (d/entity agg :root))

(defn id [agg]
  (-> agg root :root/id))

(defn type [agg]
  (-> agg meta ::spec :type))

(defn validator [agg]
  (-> agg meta ::spec :validator))

(def ^{:arglists '([query & inputs])} q d/q)

(def root-q '{:find [[?e ...]]
              :where [[?e :db/ident :root]]})

(defn build [spec]
  (let [schema       (:schema spec)
        id-generator (:id-generator spec)
        tx           (cond-> []
                       :always             (conj [:db/add 1 :db/ident :root])
                       (ifn? id-generator) (conj [:db/add :root :root/id (id-generator)]))]
    (-> (d/empty-db schema)
        (d/db-with tx)
        (vary-meta assoc ::spec spec))))

(def allow-everething
  (fn [report] nil))

(defn allow-attributes [attrs]
  (fn [{:keys [tx-data] :as report}]
    (let [attrs     (set attrs)
          all-attrs (->> tx-data
                         (map (fn [[e a v tx added]] a))
                         (set))
          denied    (set/difference all-attrs attrs)]
      (if (not-empty denied)
        (throw (ex-info "Wrong transaction"
                        {:type    ::denied-attributes
                         :denied  denied
                         :allowed attrs
                         :report  report}))))))

(defn change [agg tx-data tx-validator]
  (let [report (d/with agg tx-data)]
    (tx-validator report)
    (:db-after report)))

(defn validate
  ([agg]
   (validate agg d.validation/null-validator))
  ([agg additional-validator]
   (let [v (d.validation/compose (validator agg)
                                 additional-validator)]
     (d.validation/validate agg v))))
