(ns publicator.persistence.storage.post-mapper
  (:require
   [hugsql.core :as hugsql]
   [hugsql.adapter.clojure-jdbc :as cj-adapter]
   [publicator.domain.aggregates.post :as post]
   [publicator.persistence.storage :as persistence.storage])
  (:import
   [publicator.domain.aggregates.post Post]
   [java.sql Timestamp]))

(hugsql/def-db-fns "publicator/persistence/storage/post_mapper.sql"
  {:adapter (cj-adapter/hugsql-adapter-clojure-jdbc)})

(defn- sql->version [raw]
  (.getValue raw))

(defn- sql->aggretate [raw]
  (-> raw
      (update :created-at #(.toInstant %))
      (post/map->Post)))

(defn- aggregate->sql [aggregate]
  (-> aggregate
      (update :created-at #(Timestamp/from %))
      (vals)))

(defn- row->versioned-aggregate [row]
  {:aggregate (-> row (dissoc :version) sql->aggretate)
   :version   (-> row (get :version) sql->version)})

(defn- row->versioned-id [{:keys [id version]}]
  {:id      id
   :version (sql->version version)})

(deftype PostMapper []
  persistence.storage/Mapper
  (-lock [_ conn ids]
    (map row->versioned-id (post-locks conn {:ids ids})))
  (-select [_ conn ids]
    (map row->versioned-aggregate (post-select conn {:ids ids})))
  (-insert [_ conn aggregates]
    (post-insert conn {:vals (map aggregate->sql aggregates)}))
  (-delete [_ conn ids]
    (post-delete conn {:ids ids})))

(defn mapper []
  {Post (PostMapper.)})
