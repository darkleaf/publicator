(ns publicator.fake.post-queries
  (:require
   [publicator.interactors.abstractions.post-queries :as post-q]
   [publicator.domain.post])
  (:import
   [publicator.domain.post Post]))

(defn- list-item-projection [db post]
  (let [proj   (select-keys post [:id :title :author-id])
        author @(get @db (:author-id post))]
    (assoc proj :author-full-name (:full-name author))))

(deftype GetList [db]
  post-q/GetList
  (-get-list [_]
    (->> db
         (deref)
         (vals)
         (map deref)
         (filter #(instance? Post %))
         (map #(list-item-projection db %)))))

(defn binding-map [db]
  {#'post-q/*get-list* (->GetList db)})
