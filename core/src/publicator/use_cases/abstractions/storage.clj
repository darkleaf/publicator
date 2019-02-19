(ns publicator.use-cases.abstractions.storage)

(declare ^{:dynamic  true
           :arglists '([func])}
         atomic-apply)

(defmacro atomic [t-name & body]
  `(atomic-apply (fn [~t-name] ~@body)))

(defprotocol Transaction
  (get-many [this ids])
  (create [this state]))

(defn get-one [t id])
(defn preload [t ids])
