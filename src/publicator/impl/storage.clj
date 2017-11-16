(ns publicator.impl.storage
  (:require
   [jdbc.core :as jdbc]
   [hugsql.core :as hugsql]
   [medley.core :as medley]
   [publicator.interactors.abstractions.storage :as storage])
  (:import
   [java.util.concurrent TimeoutException]))

(defprotocol Manager
  (-lock [this conn ids])
  (-select [this conn ids])
  (-insert [this conn boxes])
  (-delete [this conn ids]))

(defn lock [this conn ids]
  (if (empty? ids)
    []
    (-lock this conn ids)))

(defn select [this conn ids]
  (if (empty? ids)
    []
    (-select this conn ids)))

(defn insert [this conn boxes]
  (if (empty? boxes)
    nil
    (-insert this conn boxes)))

(defn delete [this conn ids]
  (if (empty? ids)
    nil
    (-delete this conn ids)))

;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(deftype AggregateBox [volatile initial id version]
  clojure.lang.IDeref
  (deref [_] @volatile)

  storage/AggregateBox
  (-set! [_ new] (vreset! volatile new))
  (-id [_] id)
  (-version [_] version))

(defn build-box [state initial id version]
  (AggregateBox. (volatile! state) initial id version))

(defn- need-insert? [box]
  (and (some? @box)
       (not= @box (.-initial box))))

(defn- need-delete? [box]
  (and (some? (.-initial box))
       (or (nil? @box)
           (not= @box (.-initial box)))))

;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(deftype Transaction [data-source managers boxes]
  storage/Transaction
  (-get-many [this ids]
    (with-open [conn (jdbc/connection data-source)]
      (let [for-fetch (remove #(contains? @boxes %) ids)
            from-db   (->> managers
                           (vals)
                           (mapcat #(select % conn for-fetch))
                           (group-by storage/id)
                           (medley/map-vals first))]
        (vswap! boxes merge from-db)
        (select-keys @boxes ids))))

  (-create [this state]
    (let [id (:id state)
          it (build-box state nil id nil)]
      (vswap! boxes assoc id it)
      it)))

(defn- build-tx [data-source managers]
  (Transaction. data-source managers (volatile! {})))

(defn- lock-all [conn managers boxes]
  (let [ids      (->> (vals boxes)
                      (filter need-delete?)
                      (map storage/id))
        versions (->> managers
                      (vals)
                      (mapcat #(lock % conn ids))
                      (group-by :id)
                      (medley/map-vals #(-> % first :version)))]
    (every?
     #(let [initial (->> % (get boxes) (storage/version))
            current (get versions %)]
        (= initial current))
     ids)))

(defn- delete-all [conn managers boxes]
  (let [for-delete (filter need-delete? (vals boxes))
        groups     (->> for-delete
                        (group-by #(-> % .-initial class))
                        (medley/map-vals #(map storage/id %))
                        (medley/map-keys #(get managers %)))]
    (doseq [[manager ids] groups]
      (delete manager conn ids))))

(defn- insert-all [conn managers boxes]
  (let [for-insert (filter need-insert? (vals boxes))
        groups     (->> for-insert
                        (group-by #(-> % deref class))
                        (medley/map-keys #(get managers %)))]
    (doseq [[manager boxes] groups]
      (insert manager conn boxes))))

(defn- commit [tx managers]
  (let [data-source (.-data-source tx)
        boxes     @(.-boxes tx)]
    (with-open [conn (jdbc/connection data-source)]
      (jdbc/atomic conn
                   (when (lock-all conn managers boxes)
                     (delete-all conn managers boxes)
                     (insert-all conn managers boxes)
                     true)))))

(defn- timestamp []
  (inst-ms (java.util.Date.)))

(defn- wrap-tx [data-source managers body stop-after]
  (let [tx       (build-tx data-source managers)
        res      (body tx)
        success? (commit tx managers)]
    (cond
      success? res
      (< (timestamp) stop-after) (recur data-source managers body stop-after)
      :else (throw (TimeoutException. "Can't retry transaction")))))

(deftype Storage [data-source managers opts]
  storage/Storage
  (-wrap-tx [this body]
    (let [soft-timeout (get opts :soft-timeout 500)
          stop-after   (+ (timestamp) soft-timeout)]
      (wrap-tx data-source managers body stop-after))))

(defn binding-map [data-source managers opts]
  {:pre [(map? managers)
         (every? class? (keys managers))
         (every? #(satisfies? Manager %) (vals managers))
         (map? opts)]}
  {#'storage/*storage* (Storage. data-source managers opts)})
