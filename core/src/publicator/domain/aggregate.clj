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

(def ^{:arglists '([query & inputs])} q d/q)

(def root-q '{:find [[?e ...]]
              :where [[?e :db/ident :root]]})

#_(def base-spec
    {:schema        {:root/id {:db/unique :db.unique/identity}}
     :defaults-tx   (fn [] [[:db/add 1 :db/ident :root]
                            [:db/add :root :root/created-at (instant/*now*)]])
     :additional-tx (fn [] [[:db/add :root :root/updated-at (instant/*now*)]])
     :validator     (d.validation/compose

                     (d.validation/predicate
                      [[:root/id         pos-int?]
                       [:root/created-at inst?]
                       [:root/updated-at inst?]])

                     (d.validation/required
                      root-q #{:root/id :root/created-at :root/updated-at})

                     (d.validation/read-only
                      root-q #{:root/id :root/created-at}))})

#_(defn extend-spec [spec other]
    (cond-> spec
      (contains? other :type)
      (assoc :type (:type other))

      :always
      (update :schema merge (:schema other))

      (not (contains? spec :defaults-tx))
      (assoc :defaults-tx (fn [] []))

      (contains? other :defaults-tx)
      (update :defaults-tx (fn [old] #(concat (old) ((:defaults-tx other)))))

      (not (contains? spec :additional-tx))
      (assoc :additional-tx (fn [] []))

      (contains? other :additional-tx)
      (update :additional-tx (fn [old] #(concat (old) ((:additional-tx other)))))

      (not (contains? spec :validator))
      (assoc :validator d.validation/null-validator)

      (contains? other :validator)
      (update :validator d.validation/compose (:validator other))))

#_(defn- check-errors! [agg]
    (let [errs (-> agg meta :aggregate/errors)]
      (if (not-empty errs)
        (throw (ex-info "Aggregate has errors" {:type   ::has-errors
                                                :errors errs}))
        agg)))

(defn build [spec]
  (let [schema       (:schema spec)
        id-generator (:id-generator spec)
        tx           (cond-> []
                       :always             (conj [:db/add 1 :db/ident :root])
                       :always             (conj [:db/add :root :root/created-at (instant/*now*)])
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


(defn change [agg tx-data tx-validator!]
  (let [report (d/with agg tx-data)]
    (tx-validator! report)
    (:db-after report)))

;; (defn change [agg tx-data]
;;   (let [spec    (-> agg meta :aggregate/spec)
;;         tx-data (concat tx-data
;;                         ((:additional-tx spec)))
;;         report  (d/with agg tx-data)
;;         agg     (:db-after report)
;;         tx-data (:tx-data report)
;;         errors  (d.validation/validate report (:validator spec))
;;         agg     (vary-meta agg merge {:aggregate/tx-data tx-data
;;                                       :aggregate/errors  errors})]
;;     agg))
