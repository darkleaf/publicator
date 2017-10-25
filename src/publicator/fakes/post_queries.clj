(ns publicator.fakes.post-queries
  (:require
   [publicator.interactors.abstractions.post-queries :as post-q])
  (:import
   [publicator.domain.post Post]))

(defn- post->post-list-projection [db post]
  (let [attrs        (select-keys post [:id :title])
        author       @(get @db (:author-id post))
        author-attrs (select-keys author [:full-name])]
    (merge attrs {:author author-attrs})))

(deftype GetList [db]
  post-q/GetList
  (-get-list [_]
    (->> db
         (deref)
         (vals)
         (map deref)
         (filter #(instance? Post %))
         (map #(post->post-list-projection db %)))))

(defn binging-map [db]
  {#'post-q/*get-list* (->GetList db)})
