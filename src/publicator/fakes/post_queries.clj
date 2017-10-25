(ns publicator.fakes.post-queries
  (:require
   [publicator.interactors.abstractions.post-queries :as post-q])
  (:import
   [publicator.domain.post Post]))

(deftype GetList [db]
  post-q/GetList
  (-get-list [_]
    (->> db
         (deref)
         (vals)
         (map deref)
         (filter #(instance? Post %)))))

(defn binging-map [db]
  {#'post-q/*get-list* (->GetList db)})
