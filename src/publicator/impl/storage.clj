(ns publicator.impl.storage
  (:require
   [jdbc.core :as jdbc]
   [hugsql.core :as hugsql]
   [medley.core :as medley]
   [publicator.interactors.abstractions.storage :as storage]
   [publicator.domain.user :as user])
  (:import
   [publicator.domain.user User]))

;; (defprotocol Storage
;;   (-wrap-tx [this body]))

;; (defprotocol Transaction
;;   (-get-many [this ids])
;;   (-create [this state]))

(deftype AggregateBox [volatile initial id version]
  clojure.lang.IDeref
  (deref [_] @volatile)

  storage/AggregateBox
  (-set! [_ new] (vreset! volatile new))
  (-id [_] id)
  (-version [_] version))

(defn- build-box [state initial id version]
  (AggregateBox. (volatile! state) initial id version))


(defn need-insert? [box]
  (and (some? @box)
       (not= @box (.-initial box))))

(defn need-delete? [box]
  (and (some? (.-initial box))
       (or (nil? @box)
           (not= @box (.-initial box)))))

;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(defmulti select-for (fn [klass conn ids] klass))
(defmulti insert-for (fn [klass conn boxes] klass))
(defmulti delete-for (fn [klass conn ids] klass))

;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(defn select-all [data-source ids]
  (with-open [conn (jdbc/connection data-source)]
    (reduce-kv
     (fn [acc klass method]
       (into acc (method klass conn ids)))
     []
     (methods select-for))))

(deftype Transaction [data-source boxes]
  storage/Transaction
  (-get-many [this ids]
    (let [for-fetch     (remove #(contains? @boxes %) ids)
          from-db       (select-all data-source ids)
          indexed       (->> from-db
                             (group-by storage/id)
                             (medley/map-vals first))
          _             (vswap! boxes merge indexed)
          not-found-ids (remove #(contains? @boxes %) ids)
          indexed       (->> not-found-ids
                             (map #(build-box nil nil % nil))
                             (group-by storage/id)
                             (medley/map-vals first))
          _             (vswap! boxes merge indexed)]
      (map #(get @boxes %) ids)))

  (-create [this state]
    (let [id (:id state)
          it (build-box state nil id nil)]
      (vswap! boxes assoc id it)
      it)))

(defn build-tx [data-source]
  (Transaction. data-source (volatile! {})))

;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(defn- lock! [conn tx-val]) ;;select xmin for update

(defn delete! [conn boxes]
  (let [for-delete (filter need-delete? (vals boxes))
        groups     (->> for-delete
                        (group-by #(-> % .-initial class))
                        (medley/map-vals #(map storage/id %)))]
    (doseq [[klass ids] groups]
      (delete-for klass conn ids))))

(defn insert! [conn boxes]
  (let [for-insert (filter need-insert? (vals boxes))
        groups     (->> for-insert
                        (group-by #(-> % deref class)))]
    (doseq [[klass boxes] groups]
      (insert-for klass conn boxes))))

(defn commit! [tx]
  (let [data-source (.-data-source tx)
        boxes     @(.-boxes tx)]
    (with-open [conn (jdbc/connection data-source)]
      (jdbc/atomic conn
                   (lock! conn boxes)
                   (delete! conn boxes)
                   (insert! conn boxes)))))

(defn wrap-tx [data-source body]
  (let [tx  (build-tx data-source)
        res (body tx)]
    (commit! tx)
    res))

(deftype Storage [data-source]
  storage/Storage
  (-wrap-tx [this body]
    ;;todo retry
    (wrap-tx data-source body)))

(defn binding-map [data-source]
  {#'storage/*storage* (Storage. data-source)})


;;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(hugsql/def-db-fns "db/storage/user.sql" {:quoting :ansi})

(defmethod insert-for User [_ conn boxes]
  (user-insert conn {:vals (map #(-> % deref vals) boxes)}))

(defn- row->user-box [row]
  (let [version (-> row :version .getValue)
        row     (dissoc row :version)
        user    (user/map->User row)]
     (build-box user user (:id user) version)))

(defmethod select-for User [_ conn ids]
  (map row->user-box (user-select conn {:ids ids})))

(defmethod delete-for User [_ conn ids]
  (user-delete conn {:ids ids}))
