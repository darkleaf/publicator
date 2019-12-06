(ns publicator.util
  (:refer-clojure :exclude [type])
  (:require
   [clojure.walk :as w]
   [clojure.test :as t]))

(defmacro <<- [& body]
  `(->> ~@(reverse body)))

(defmacro fn-> [& body]
  `(fn [arg#] (-> arg# ~@body)))

(defn distinct-coll? [coll]
  (= coll (distinct coll)))

(defn same-items? [coll expected]
  (= (sort coll)
     (sort expected)))

(defn map-vals [f m]
  (reduce-kv
   (fn [acc k v] (assoc acc k (f v)))
   {} m))

(defn map-keys [f m]
  (reduce-kv
   (fn [acc k v] (assoc acc (f k) v))
   {} m))

(defn reverse-merge [& maps]
  (apply merge (reverse maps)))

(defn type [x]
  (-> x meta :type))
