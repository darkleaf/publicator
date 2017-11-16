(ns publicator.impl.storage.post-manager
  (:require
   [hugsql.core :as hugsql]
   [publicator.domain.post :as post]
   [publicator.impl.storage :as impl.storage])
  (:import
   [publicator.domain.post Post]))

(hugsql/def-db-fns "publicator/impl/storage/post_manager.sql")

(defn- get-version [row]
  (-> row :version .getValue))

(defn- row->box [row]
  (let [version (get-version row)
        row     (dissoc row :version)
        entity  (post/map->Post row)]
     (impl.storage/build-box entity entity (:id entity) version)))

(defn- lock-row->map [row]
  (let [id      (:id row)
        version (get-version row)]
    {:id id, :version version}))

(deftype PostManager []
  impl.storage/Manager
  (-lock [_ conn ids]
    (map lock-row->map (post-locks conn {:ids ids})))
  (-select [_ conn ids]
    (map row->box (post-select conn {:ids ids})))
  (-insert [_ conn boxes]
    (post-insert conn {:vals (map #(-> % deref vals) boxes)}))
  (-delete [_ conn ids]
    (post-delete conn {:ids ids})))


(defn manager []
  {Post (PostManager.)})
