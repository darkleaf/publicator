(ns publicator-ext.util
  (:refer-clojure :exclude [distinct?]))

(defn distinct? [coll]
  (= coll (distinct coll)))

(defn match? [coll expected]
  (= (sort coll)
     (sort expected)))
