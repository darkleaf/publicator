(ns publicator-ext.domain.abstractions.aggregate
  (:require
   [publicator-ext.domain.abstractions.instant :as instant]
   [publicator-ext.domain.util.validation :as validation]
   [datascript.core :as d]))

(defmulti schema identity)
(defmethod schema :default [_] {})

(defmulti validator (fn [chain] (-> chain validation/aggregate type)))
(defmethod validator :default [chain] chain)

(defn- common-validator [chain]
  (-> chain
      (validation/types [:aggregate/id         pos-int?]
                        [:aggregate/created-at inst?]
                        [:aggregate/updated-at inst?])
      (validation/required-for '{:find [[?e ...]]
                                 :where [[?e :db/ident :root]]}
                               [:aggregate/id         some?]
                               [:aggregate/created-at some?]
                               [:aggregate/updated-at some?])))

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
                 {:aggregate/id {:db/unique :db.unique/identity}})]
    (-> (d/empty-db s)
        (d/db-with [{:db/ident     :root
                     :aggregate/id id}])
        (with-meta {:type type}))))

(defn build [type id tx-data]
   (let [aggregate (-> (allocate type id)
                       (d/db-with [[:db/add :root :aggregate/created-at (instant/now)]
                                   [:db/add :root :aggregate/updated-at (instant/now)]])
                       (d/db-with tx-data))]
     (doto aggregate
       check-errors!)))

(defn change [aggregate tx-data]
  (let [aggregate (-> aggregate
                      (d/db-with tx-data)
                      (d/db-with [[:db/add :root :aggregate/updated-at (instant/now)]]))]
    (doto aggregate
      check-errors!)))
