(ns publicator.domain.aggregate
  (:require
   [publicator.domain.abstractions.instant :as instant]
   [publicator.utils.datascript.validation :as d.validation]
   [datascript.core :as d]))

(defn root [aggregate]
  (d/entity aggregate :root))

(def ^{:arglists '([query & inputs])} q d/q)

(def ^:const root-q '{:find [[?e ...]]
                      :where [[?e :db/ident :root]]})

(def base-spec
  {:schema        {:root/id {:db/unique :db.unique/identity}}
   :defaults-tx   (fn [] [[:db/add 1 :db/ident :root]
                          [:db/add :root :root/created-at (instant/*now*)]])
   :additional-tx (fn [] [[:db/add :root :root/updated-at (instant/*now*)]])
   :read-only     #{:root/id :root/created-at}
   :validator     (d.validation/compose
                   (d.validation/attributes [:root/id         pos-int?]
                                            [:root/created-at inst?]
                                            [:root/updated-at inst?])
                   (d.validation/in-case-of root-q
                                            [:root/id         some?]
                                            [:root/created-at some?]
                                            [:root/updated-at some?]))})

(defn extend-spec [spec other]
  (cond-> spec
    (contains? other :type)          (assoc :type (:type other))
    :always                          (update :schema
                                             merge (:schema other))
    (contains? other :defaults-tx)   (update :defaults-tx
                                             (fn [old] #(concat (old) ((:defaults-tx other)))))
    (contains? other :additional-tx) (update :additional-tx
                                             (fn [old] #(concat (old) ((:additional-tx other)))))
    :always                          (update :read-only
                                             into (:read-only other))
    (contains? other :validator)     (update :validator
                                             d.validation/compose (:validator other))))

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
        changes (:tx-data report)
        errors  (d.validation/validate agg (:validator spec))
        agg (vary-meta agg merge {:type              (:type spec)
                                  :aggregate/spec    spec
                                  :aggregate/changes changes
                                  :aggregate/errors  errors})]
    (check-errors! agg)))

(defn- check-read-only! [agg]
  (let [read-only (-> agg meta :aggregate/spec :read-only)
        changes   (-> agg meta :aggregate/changes)
        violators (->> changes
                       (map (fn [[e a v t added]] a))
                       (filter #(contains? read-only %))
                       (set))]
    (if (not-empty violators)
      (throw (ex-info "Read only attributes are changed" {:type      ::read-only-violation
                                                          :violators violators}))
      agg)))

(defn change [agg tx-data]
  (let [spec    (-> agg meta :aggregate/spec)
        tx-data (concat tx-data
                        ((:additional-tx spec)))
        report  (d/with agg tx-data)
        agg     (:db-after report)
        changes (:tx-data report)
        errors  (d.validation/validate agg (:validator spec))
        agg     (vary-meta agg merge {:aggregate/changes changes
                                      :aggregate/errors  errors})]
    (-> agg
        (check-read-only!)
        (check-errors!))))
