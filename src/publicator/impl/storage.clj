(ns publicator.impl.storage
  (:require
   [jdbc.core :as jdbc]
   [hugsql.core :as hugsql]
   [medley.core :as medley]
   [slingshot.slingshot :refer [try+ throw+]]
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

(defmulti locks-for  (fn [klass conn ids] klass))
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

(defn- locks-all [conn ids]
  (if (empty? ids)
    {}
    (do
      (reduce-kv
       (fn [acc klass method]
         (merge acc (method klass conn ids)))
       {}
       (methods locks-for)))))

(defn- check-locks! [conn boxes]
  (let [ids      (->> (vals boxes)
                      (filter need-delete?)
                      (map storage/id))
        versions (locks-all conn ids)]
    (doseq [id ids
            :let [old-version (->> id
                                   (get boxes)
                                   (storage/version))
                  cur-version (get versions id)]
             :when (not= old-version cur-version)]
      (throw+ {:type ::locks-failed}))))

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
                   (check-locks! conn boxes)
                   (delete! conn boxes)
                   (insert! conn boxes)))))

(defn wrap-tx
  ([data-source body]
   (wrap-tx data-source body 0))
  ([data-source body attempt]
   (let [tx       (build-tx data-source)
         res      (body tx)
         success? (try+
                   (commit! tx)
                   true
                   (catch [:type ::locks-failed] _
                     (if (> 25 attempt)
                       false
                       (throw+))))]
     (if success?
       res
       (recur data-source body (inc attempt))))))

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

(defn- get-version [row]
  (-> row :version .getValue))

(defn- row->user-box [row]
  (let [version (get-version row)
        row     (dissoc row :version)
        user    (user/map->User row)]
     (build-box user user (:id user) version)))

(defmethod select-for User [_ conn ids]
  (map row->user-box (user-select conn {:ids ids})))

(defmethod delete-for User [_ conn ids]
  (user-delete conn {:ids ids}))

(defn- lock-row->pair [row]
  (let [id      (:id row)
        version (get-version row)]
    [id version]))

(defmethod locks-for User [_ conn ids]
  (into {} (map lock-row->pair (user-locks conn {:ids ids}))))
