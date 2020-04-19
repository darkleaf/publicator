(ns publicator.utils
  (:refer-clojure :exclude [type #?(:cljs regexp?)])
  #?(:cljs (:require-macros [publicator.utils :refer [<<-]])))

(defmacro <<- [& body]
  `(->> ~@(reverse body)))

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

(defn regexp? [x]
  (instance? #?(:clj  java.util.regex.Pattern
                :cljs js/RegExp)
             x))
