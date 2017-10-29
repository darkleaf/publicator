(ns publicator.ring.helpers
  (:require
   [sibiro.core :as sibiro]))

(declare ^:dynamic *routes*)

(defn path-for [& xs]
  {:arglists '([handler] [handler params]
               [tag] [tag params])}
  (apply sibiro/path-for *routes* xs))
