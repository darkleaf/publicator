(ns publicator.utils
  (:refer-clojure :exclude [#?(:cljs regexp?)])
  #?(:cljs (:require-macros [publicator.utils :refer [<<-]])))

(defmacro <<- [& body]
  `(->> ~@(reverse body)))
