(ns publicator.domain.aggregate
  (:require
   [publicator.domain.abstractions.instant :as instant]
   [publicator.domain.utils.validation :as validation]
   [datascript.core :as d]))

(defmulti schema identity)
(defmethod schema :default [_] {})

(defmulti validator (fn [chain] (-> chain validation/aggregate type)))
(defmethod validator :default [chain] chain)

(def ^:const root-q '{:find [[?e ...]]
                      :where [[?e :db/ident :root]]})

(defn- common-validator [chain]
  (-> chain
      (validation/types [:root/id         pos-int?]
                        [:root/created-at inst?]
                        [:root/updated-at inst?])
      (validation/required-for root-q
                               [:root/id         some?]
                               [:root/created-at some?]
                               [:root/updated-at some?])))

(defn- check-errors! [aggregate]
  (let [errs (-> (validation/begin aggregate)
                 (common-validator)
                 (validator)
                 (validation/end))]
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
        (with-meta {:type type}))))

(defn build [type id tx-data]
   (let [aggregate (-> (allocate type id)
                       (d/db-with [[:db/add :root :root/created-at (instant/*now*)]
                                   [:db/add :root :root/updated-at (instant/*now*)]])
                       (d/db-with tx-data))]
     (doto aggregate
       check-errors!)))

(defn change [aggregate tx-data]
  (let [aggregate (-> aggregate
                      (d/db-with tx-data)
                      (d/db-with [[:db/add :root :root/updated-at (instant/*now*)]]))]
    (doto aggregate
      check-errors!)))
