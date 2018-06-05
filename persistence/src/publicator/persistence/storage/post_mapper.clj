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

(defn- get-version [row]
  (-> row :version .getValue))

(defn- row->state [row]
  (let [version (get-version row)
        row     (-> row
                    (dissoc :version)
                    (update :created-at #(.toInstant %)))
        state   (post/map->Post row)]
    {:state state, :version version}))

(defn- lock-row->map [row]
  (let [id      (:id row)
        version (get-version row)]
    {:id id, :version version}))

(defn- state->values [state]
  (-> state
      (update :created-at #(Timestamp/from %))
      vals))

(deftype PostMapper []
  persistence.storage/Mapper
  (-lock [_ conn ids]
    (map lock-row->map (post-locks conn {:ids ids})))
  (-select [_ conn ids]
    (map row->state (post-select conn {:ids ids})))
  (-insert [_ conn states]
    (post-insert conn {:vals (map state->values states)}))
  (-delete [_ conn ids]
    (post-delete conn {:ids ids})))

(defn mapper []
  {Post (PostMapper.)})
