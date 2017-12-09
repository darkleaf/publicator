(ns publicator.interactors.abstractions.storage
  (:refer-clojure :exclude [swap!])
  (:require
   [medley.core :as medley]
   [publicator.domain.protocols.aggregate :as aggregate]))

;; Транзакция описывает единицу работы(unit of work).
;; Идентичность(identity) агрегатов моделируются AggregateBox.

;; При извлечении/создании агрегата сохраняется его начальное состояние,
;; в конце транзакции состояние агрегата сравнивается с начальным.

;; Возможно, реализация storage использует оптимистические блокировки.
;; Следовательно, тело транзакции может быть запущено несколько раз.

;; get-* должны поддерживать семантику idenitity map,
;; т.е. одному id всегда соответствует один и тот же aggregate box.


(defprotocol AggregateBox
  (-set! [this new])
  (-id [this])
  (-version [this]))

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
                (and (= (:id old) (:id new))
                     (= (class old) (class new)))))
    (-set! box new)
    new))

;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(defprotocol Storage
  (-wrap-tx [this body]))

(defprotocol Transaction
  (-get-many [this ids])
  (-create [this state]))

(declare ^:dynamic *storage*)

(defmacro with-tx
  "Note that body forms may be called multiple times,
   and thus should be free of side effects."
  [tx-name & body-forms-free-of-side-effects]
  `(-wrap-tx *storage* (fn [~tx-name] ~@body-forms-free-of-side-effects)))

(defmacro ^:private assert-idempotence [form message]
  `(let [first-result# ~form]
     (assert (= first-result# ~form) ~message)
     first-result#))

(defn get-many [tx ids]
  {:pre [(every? some? ids)]
   :post [(map? %)
          (<= (count %) (count ids))
          (every? box? (vals %))]}
  (assert-idempotence (-get-many tx ids) "Identity Map isn't implemented!"))

(defn get-one [tx id]
  {:post [((some-fn nil? box?) %)]}
  (let [res (get-many tx [id])]
    (get res id)))

(defn create [tx state]
  {:post [(box? %)]}
  (-create tx state))

;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(defn tx-get-one [id]
  (with-tx tx
    (when-let [x (get-one tx id)]
      @x)))

(defn tx-get-many [ids]
  (with-tx tx
    (->> ids
         (get-many tx)
         (medley/map-vals deref))))

(defn tx-create [state]
  (with-tx t
    @(create t state)))

(defn tx-swap! [id f & args]
  (with-tx t
    (when-let [x (get-one t id)]
      (apply swap! x f args))))

(defn tx-destroy! [id]
  (with-tx t
    (let [x (get-one t id)]
      (destroy! x))))
