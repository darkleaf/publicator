(ns publicator.persistence.storage.post-mapper
  (:require
   [hugsql.core :as hugsql]
   [publicator.domain.aggregates.post :as post]
   [publicator.persistence.storage :as persistence.storage])
  (:import
   [publicator.domain.aggregates.post Post]))

(hugsql/def-db-fns "publicator/persistence/storage/post_mapper.sql")

(defn- row->versioned-aggregate [row]
  {:aggregate (-> row (dissoc :version) post/map->Post)
   :version   (-> row (get :version))})

(deftype PostMapper []
  persistence.storage/Mapper
  (-lock [_ conn ids]
    (post-locks conn {:ids ids}))
  (-select [_ conn ids]
    (map row->versioned-aggregate (post-select conn {:ids ids})))
  (-insert [_ conn aggregates]
    (post-insert conn {:vals (map vals aggregates)}))
  (-delete [_ conn ids]
    (post-delete conn {:ids ids})))

(defn mapper []
  {Post (PostMapper.)})
