(ns publicator.use-cases.abstractions.storage
  (:require
   [publicator.utils.coll :as u.c]))

(declare ^{:dynamic true, :arglists '([func-from-t])}
         *atomic-apply*)

(defprotocol Transaction
  :extend-via-metadata true
  (create [t state])
  (get-many [t ids]))

(defmacro atomic
  {:style/indent [1 [[:defn]] :form]}
  [t-name & body]
  `(*atomic-apply* (fn [~t-name] ~@body)))

(defn get-one [t id]
  (let [res (get-many t [id])]
    (get res id)))

(def ^{:arglists '([t ids])} preload get-many)

(defn just-get-one [id]
  (atomic t
    (when-let [x (get-one t id)]
      @x)))

(defn just-get-many [ids]
  (atomic t
    (->> ids
         (get-many t)
         (u.c/map-vals deref))))

(defn just-create [state]
  (atomic t
    (create t state))
  nil)

(defn just-alter [id f & args]
  (atomic t
    (when-let [x (get-one t id)]
      (dosync
       (apply alter x f args)))))
