(ns publicator.use-cases.abstractions.storage
  (:require
   [publicator.utils.coll :as u.c]))

(declare ^{:dynamic true, :arglists '([func-from-t])}
         *atomic-apply*)

(defprotocol Transaction
  "Thread unsafe"
  :extend-via-metadata true
  (create [t state])
  (get-many [t type ids]))

(defmacro atomic
  {:style/indent [1 [[:defn]] :form]}
  [t-name & body]
  `(*atomic-apply* (fn [~t-name] ~@body)))

(defn get-one [t type id]
  (let [res (get-many t type [id])]
    (get res id)))

(def ^{:arglists '([t type ids])} preload get-many)

(defn just-get-one [type id]
  (atomic t
    (when-let [x (get-one t type id)]
      @x)))

(defn just-get-many [type ids]
  (atomic t
    (->> ids
         (get-many t type)
         (u.c/map-vals deref))))

(defn just-create [state]
  (atomic t
    (create t state))
  nil)

(defn just-alter [type id f & args]
  (atomic t
    (when-let [x (get-one t type id)]
      (dosync
       (apply alter x f args)))))
