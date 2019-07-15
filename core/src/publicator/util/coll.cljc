(ns publicator.util.coll
  (:refer-clojure :exclude [distinct?]))

(defn distinct? [coll]
  (= coll (distinct coll)))

;; (defn map-vals [f m]
;;   (reduce-kv
;;    (fn [acc k v] (assoc acc k (f v)))
;;    {} m))

;; (defn map-keys [f m]
;;   (reduce-kv
;;    (fn [acc k v] (assoc acc (f k) v))
;;    {} m))

;; (defn reverse-merge [& maps]
;;   (apply merge (reverse maps)))
