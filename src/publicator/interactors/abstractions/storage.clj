(ns publicator.interactors.abstractions.storage
  (:require
   [publicator.domain.abstractions.aggregate :as aggregate]
   [clojure.spec.alpha :as s]))

;; Транзакция описывает единицу работы(unit of work).
;; Идентичность(identity) агрегатов моделируются атомами.
;; При извлечении/создании агрегата сохраняется его начальное состояние,
;; в конце транзакции состояние агрегата сравнивается с начальным.

;; Возможно, что реализация использует оптимистические блокировки.
;; Следовательно, тело транзакции может быть запущено несколько раз.

;; get-* должны поддерживать семантику idenitity map,
;; т.е. одному id всегда соответствует один и тот же атом.

;; Внутри http запроса может быть несколько транзакций.
;; На каждый http запрос должен быть свой кэш.

(defprotocol Storage
  (-wrap-tx [this body]))

(defprotocol Transaction
  (-get-many [this ids])
  (-create [this state]))

(declare ^:dynamic *storage*)

(defmacro with-tx [tx-name & body]
  `(-wrap-tx *storage* (fn [~tx-name] ~@body)))

(defn- atom? [x] (instance? clojure.lang.Atom x))

(defn- aggregate-validator [state]
  (if state
    (s/assert (aggregate/spec state) state)
    true))

(defn get-many [tx ids]
  {:post [(every? atom? %)]}
  (let [res (-get-many tx ids)]
    (doseq [x res] (set-validator! x aggregate-validator))
    res))

(defn get-one [tx id]
  {:post [((some-fn nil? atom?) %)]}
  (first (get-many tx [id])))

(defn create [tx state]
  {:post [(atom? %)]}
  (let [res (-create tx state)]
    (set-validator! res aggregate-validator)
    res))

(defn tx-get-one [id]
  (with-tx tx
    (when-let [x (get-one tx id)]
      @x)))

(defn tx-get-many [ids]
  (with-tx tx
    (->> ids
         (get-many tx)
         (map deref))))

(defn tx-create [state]
  (with-tx t
    @(create t state)))
