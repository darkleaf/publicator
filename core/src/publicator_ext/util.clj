(ns publicator-ext.util)

(defn distinct? [coll]
  (= coll (distinct coll)))

(defn match? [coll expected]
  (= (sort coll)
     (sort expected)))
