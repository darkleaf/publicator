(ns publicator.util
  (:refer-clojure :exclude [type])
  (:require
   [clojure.walk :as w]
   [clojure.test :as t]))

(defn- inject* [next form]
  (let [injected (w/prewalk-replace {'<> next} form)]
    (cond
      (not= form injected) injected
      (seq? form) `(~@form ~next)
      :else                (throw (ex-info "This form must contain <> or be a seq"
                                           {:form form, :next next})))))

(defn- linearize* [body]
  (reduce inject* (reverse body)))

(defmacro linearize [& body]
  (linearize* body))

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

(defn test-with-script [continuation script]
  (loop [[actual-effect continuation]       [nil continuation]
         [{:keys [effect coeffect]} & tail] script]
    (t/is (= effect actual-effect))
    (if (nil? continuation)
      (t/is (empty? tail))
      (recur (continuation coeffect)
             tail))))
