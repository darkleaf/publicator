(ns publicator.domain.aggregate
  (:require
   [publicator.domain.abstractions.instant :as instant]
   [publicator.utils.datascript.validation :as d.validation]
   [datascript.core :as d]))

(defn root [aggregate]
  (d/entity aggregate :root))

(def ^{:arglists '([query & inputs])} q d/q)

(def root-q '{:find [[?e ...]]
              :where [[?e :db/ident :root]]})

(def base-spec
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

(defn extend-spec [spec other]
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

(defn extend-agg [agg spec]
  (vary-meta agg update :aggregate/spec extend-spec spec))

(defn- check-errors! [agg]
  (let [errs (-> agg meta :aggregate/errors)]
    (if (not-empty errs)
      (throw (ex-info "Aggregate has errors" {:type   ::has-errors
                                              :errors errs}))
      agg)))

(defn build [spec tx-data]
  (let [spec    (extend-spec base-spec spec)
        tx-data (concat ((:defaults-tx spec))
                        tx-data
                        ((:additional-tx spec)))
        agg     (d/empty-db (:schema spec))
        report  (d/with agg tx-data)
        agg     (:db-after report)
        tx-data (:tx-data report)
        errors  (d.validation/validate report (:validator spec))
        agg     (vary-meta agg merge {:type              (:type spec)
                                      :aggregate/spec    spec
                                      :aggregate/tx-data tx-data
                                      :aggregate/errors  errors})]
    agg))

(defn build! [spec tx-data]
  (-> (build spec tx-data)
      (check-errors!)))

(defn change [agg tx-data]
  (let [spec    (-> agg meta :aggregate/spec)
        tx-data (concat tx-data
                        ((:additional-tx spec)))
        report  (d/with agg tx-data)
        agg     (:db-after report)
        tx-data (:tx-data report)
        errors  (d.validation/validate report (:validator spec))
        agg     (vary-meta agg merge {:aggregate/tx-data tx-data
                                      :aggregate/errors  errors})]
    agg))

(defn change! [agg tx-data]
  (-> (change agg tx-data)
      (check-errors!)))
