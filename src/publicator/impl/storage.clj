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

(defn select-all [with-conn ids]
  (with-conn
    (fn [conn]
      (reduce-kv
       (fn [acc k f]
         (into acc (f k conn ids)))
       []
       (methods select-for)))))

(deftype Transaction [with-conn boxes]
  storage/Transaction
  (-get-many [this ids]
    (let [for-fetch     (remove #(contains? @boxes %) ids)
          from-db       (select-all with-conn ids)
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

(defn build-tx [with-conn]
  (Transaction. with-conn (volatile! {})))

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
  (let [with-conn (.-with-conn tx)
        boxes     @(.-boxes tx)]
    (with-conn
      (fn [conn]
        (jdbc/atomic
         conn
         (lock! conn boxes)
         (delete! conn boxes)
         (insert! conn boxes))))))

(defn wrap-tx [with-conn body]
  (let [tx  (build-tx with-conn)
        res (body tx)]
    (commit! tx)
    res))

(deftype Storage [with-conn]
  storage/Storage
  (-wrap-tx [this body]
    ;;todo retry
    (wrap-tx with-conn body)))

(defn binding-map [with-conn]
  {#'storage/*storage* (Storage. with-conn)})


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
