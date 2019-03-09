(ns publicator.utils.string)

(defn match? [s re]
  (re-matches re s))
