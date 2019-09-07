(ns publicator.util
  (:require
   [clojure.walk :as w]))

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

(defmacro or-some
  ([] nil)
  ([x] x)
  ([x & next] `(if-some [or# ~x] or# (or-some ~@next))))

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

(defn regexp? [x]
  #?(:clj  (instance? java.util.regex.Pattern x)
     :cljs (instance? js/RegExp x)))
