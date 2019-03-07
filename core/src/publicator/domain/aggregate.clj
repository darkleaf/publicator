(ns publicator.domain.aggregate
  (:require
   [publicator.domain.abstractions.instant :as instant]
   [publicator.domain.aggregate.validation :as validation]
   [datascript.core :as d]))

(defmulti schema identity)
(defmethod schema :default [_] {})

(defmulti validator type)
(defmethod validator :default [agg] agg)

(def ^:const root-q '{:find [[?e ...]]
                      :where [[?e :db/ident :root]]})

(defn- common-validator [agg]
  (-> agg
      (validation/attributes [:root/id         pos-int?]
                             [:root/created-at inst?]
                             [:root/updated-at inst?])
      (validation/in-case-of root-q
                             [:root/id         some?]
                             [:root/created-at some?]
                             [:root/updated-at some?])))

(defn- check-errors! [aggregate]
  (let [errs (->  aggregate
                  (common-validator)
                  (validator)
                  (meta)
                  :aggregate/errors)]
    (if (not-empty errs)
      (throw (ex-info "Aggregate has errors" {:type   ::has-errors
                                              :errors errs})))))

(defn root [aggregate]
  (d/entity aggregate :root))

(defn allocate [type id]
  (let [s (merge (schema type)
                 {:root/id {:db/unique :db.unique/identity}})]
    (-> (d/empty-db s)
        (d/db-with [{:db/ident :root
                     :root/id  id}])
        (vary-meta assoc
                   :type type
                   :aggregate/errors (d/empty-db)))))

(defn build [type id tx-data]
  (let [tx-data   (concat tx-data
                          [[:db/add :root :root/created-at (instant/*now*)]
                           [:db/add :root :root/updated-at (instant/*now*)]
                           [:db.fn/call check-errors!]])
        aggregate (allocate type id)
        report    (d/with aggregate tx-data)
        tx-data   (:tx-data report)
        aggregate (:db-after report)]
    (vary-meta aggregate assoc :aggregate/tx-data tx-data)))

(defn change [aggregate tx-data]
  (let [tx-data   (concat tx-data
                          [[:db/add :root :root/updated-at (instant/*now*)]
                           [:db.fn/call check-errors!]])
        report    (d/with aggregate tx-data)
        tx-data   (:tx-data report)
        aggregate (:db-after report)]
    (vary-meta aggregate assoc :aggregate/tx-data tx-data)))
