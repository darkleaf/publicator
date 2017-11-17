(ns publicator.ring.helpers
  (:require
   [sibiro.core :as sibiro]))

(declare ^:dynamic *routes*)

(defn path-for
  {:arglists '([handler] [handler params]
               [tag] [tag params])}
  [& xs]
  (apply sibiro/path-for *routes* xs))
