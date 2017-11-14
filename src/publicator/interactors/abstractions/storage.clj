(ns publicator.interactors.abstractions.storage
  (:refer-clojure :exclude [swap!])
  (:require
   [publicator.domain.abstractions.aggregate :as aggregate]))

;; Транзакция описывает единицу работы(unit of work).
;; Идентичность(identity) агрегатов моделируются атомами.
;; При извлечении/создании агрегата сохраняется его начальное состояние,
;; в конце транзакции состояние агрегата сравнивается с начальным.

;; Возможно, что реализация использует оптимистические блокировки.
;; Следовательно, тело транзакции может быть запущено несколько раз.

;; get-* должны поддерживать семантику idenitity map,
;; т.е. одному id всегда соответствует один и тот же aggregate-box.

;; Внутри http запроса может быть несколько транзакций.
;; На каждый http запрос должен быть свой кэш.

(defprotocol Storage
  (-wrap-tx [this body]))

(defprotocol Transaction
  (-get-many [this ids])
  (-create [this state]))

(defprotocol AggregateBox
  (-set! [this new])
  (-id [this])
  (-version [this]))

(declare ^:dynamic *storage*)

(defmacro with-tx [tx-name & body]
  `(-wrap-tx *storage* (fn [~tx-name] ~@body)))

(defn box? [x]
  (and
   (satisfies? AggregateBox x)
   (instance? clojure.lang.IDeref x)))

(defn id [box]
  {:pre [(box? box)]}
  (-id box))

(defn version [box]
  {:pre [(box? box)]}
  (-version box))

(defn destroy! [box]
  {:pre [(box? box)]}
  (-set! box nil))

(defn swap! [box f & args]
  {:pre [(box? box)]}
  (let [old (aggregate/nilable-assert @box)
        new (aggregate/nilable-assert (apply f old args))]
    (assert (or (nil? old)
                (nil? new)
                (= (:id old) (:id new))
                (= (class old) (class new))))
    (-set! box new)
    new))

(defn get-many [tx ids]
  {:pre [(every? some? ids)]
   :post [(every? box? %)
          (= ids (map id %))]}
  (-get-many tx ids))

(defn get-one [tx id]
  {:post [(box? %)]}
  (first (get-many tx [id])))

(defn create [tx state]
  {:post [(box? %)]}
  (-create tx state))

(defn tx-get-one [id]
  (with-tx tx
    @(get-one tx id)))

(defn tx-get-many [ids]
  (with-tx tx
    (->> ids
         (get-many tx)
         (map deref))))

(defn tx-create [state]
  (with-tx t
    @(create t state)))

(defn tx-swap! [id f & args]
  (with-tx t
    (let [x (get-one t id)]
      (apply swap! x f args))))

(defn tx-destroy! [id]
  (with-tx t
    (let [x (get-one t id)]
      (destroy! x))))
