(ns publicator.interactors.post.list
  (:require
   [publicator.interactors.abstractions.post-queries :as post-q]))

(defn process []
  {:type ::processed
   :posts (post-q/get-list)})
