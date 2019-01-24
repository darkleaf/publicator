(ns publicator-ext.domain.abstractions.aggregate
  (:refer-clojure :exclude [type update])
  (:require
   [publicator-ext.domain.abstractions.instant :as instant]
   [datascript.core :as d]
   [clojure.spec.alpha :as s]))

(s/def :entity/type keyword?)
(s/def :aggregate/id pos-int?)
(s/def :aggregate/created-at inst?)
(s/def :aggregate/updated-at inst?)

(s/def :aggregate/entity (s/keys :req [:entity/type]))
(s/def :aggregate/root (s/keys :req [:entity/type :aggregate/id
                                     :aggregate/created-at :aggregate/updated-at]))

(def ^:const +schema+ {:aggregate/id {:db/unique :db.unique/identity}})

(defn root [aggregate]
  (d/entity aggregate :root))

(defn id [aggregate]
  (-> aggregate root :aggregate/id))

(defn type [aggregate]
  (-> aggregate root :entity/type))

(defmulti errors type)
(defmethod errors :default [_])

(defn- check-errors! [aggregate]
  (let [errs (errors aggregate)]
    (if (not-empty errs)
      (throw (ex-info "Aggregate has errors" {:type   ::has-errors
                                              :errors errs})))))

(defn- check-root! [aggregate]
  (let [root (d/pull aggregate '[*] :root)
        ed   (s/explain-data :aggregate/root root)]
    (if (some? ed)
      (throw (ex-info "Invalid aggregate root" {:type         ::invalid-root
                                                :explain-data ed})))))

(defn- check-entities! [aggregate]
  (let [entities (->> aggregate
                      (d/q '{:find  [(pull ?e [*])]
                             :where [[?e _ _]]})
                      (map first))
        eds      (->> entities
                      (map #(s/explain-data (s/and :aggregate/entity
                                                   (:entity/type %))
                                            %))
                      (remove nil?))]
    (if (not-empty eds)
      (throw (ex-info "Invalid aggregate entities" {:type         ::invalid-entities
                                                    :explain-data eds})))))

(defn build
  ([params]
   (build {} params))
  ([schema params]
   (let [schema    (merge schema +schema+)
         tx-item   (merge params
                          {:db/ident             :root
                           :aggregate/created-at (instant/now)
                           :aggregate/updated-at (instant/now)})
         aggregate (-> (d/empty-db schema)
                       (d/db-with [tx-item]))]
     (doto aggregate
       check-root!
       check-entities!
       check-errors!))))

(defn update [aggregate tx-data]
  (let [previous  aggregate
        aggregate (-> aggregate
                      (d/db-with tx-data)
                      (d/db-with [[:db/add :root :aggregate/updated-at (instant/now)]]))]
    (doto aggregate
      check-root!
      check-entities!
      check-errors!)))
