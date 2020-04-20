(ns publicator.utils
  (:refer-clojure :exclude [type #?(:cljs regexp?)])
  #?(:cljs (:require-macros [publicator.utils :refer [<<-]])))

(defmacro <<- [& body]
  `(->> ~@(reverse body)))

(defmacro fn-> [& body]
  `(fn [arg#] (-> arg# ~@body)))

(defmacro fn->> [& body]
  `(fn [arg#] (->> arg# ~@body)))

(defn type [x]
  (-> x meta :type))

(defn regexp? [x]
  (instance? #?(:clj  java.util.regex.Pattern
                :cljs js/RegExp)
             x))
