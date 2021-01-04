(ns publicator.utils
  (:refer-clojure :exclude [#?(:cljs regexp?)])
  #?(:cljs (:require-macros [publicator.utils :refer [<<-]])))

(defmacro <<- [& body]
  `(->> ~@(reverse body)))

(defn regexp? [x]
  (instance? #?(:clj  java.util.regex.Pattern
                :cljs js/RegExp) x))

(defn getx
  "Like two-argument get, but throws an exception if the key is not found."
  ([m k]
   (getx m k "Missing required key"))
  ([m k msg]
   (let [e (get m k ::sentinel)]
     (if (= e ::sentinel) (throw (ex-info msg {:map m :key k})))
     e)))

(defn getx-in
  "Like two-argument get-in, but throws an exception if the keys are not found."
  ([m ks]
   (getx-in m ks "Missing required keys"))
  ([m ks msg]
   (let [e (get-in m ks ::sentinel)]
     (if (= e ::sentinel) (throw (ex-info msg {:map m :keys ks})))
     e)))
