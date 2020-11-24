(ns publicator.utils
  (:refer-clojure :exclude [#?(:cljs regexp?)])
  #?(:cljs (:require-macros [publicator.utils :refer [<<-]])))

(defmacro <<- [& body]
  `(->> ~@(reverse body)))

(defn regexp? [x]
  (instance? #?(:clj  java.util.regex.Pattern
                :cljs js/RegExp)
             x))
