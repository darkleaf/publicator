(ns publicator.persistence.storage.user-mapper
  (:require
   [hugsql.core :as hugsql]
   [hugsql.adapter.clojure-jdbc :as cj-adapter]
   [publicator.domain.aggregates.user :as user]
   [publicator.persistence.storage :as persistence.storage])
  (:import
   [publicator.domain.aggregates.user User]
   [java.sql Timestamp]))

(hugsql/def-db-fns "publicator/persistence/storage/user_mapper.sql"
  {:adapter (cj-adapter/hugsql-adapter-clojure-jdbc)})

(defn- sql->version [raw]
  (.getValue raw))

(defn- sql->aggregate [raw]
  (-> raw
      (update :posts-ids #(-> % .getArray set))
      (update :created-at #(.toInstant %))
      (user/map->User)))

(defn- aggregate->sql [aggregate]
  (-> aggregate
      (update :posts-ids long-array)
      (update :created-at #(Timestamp/from %))
      (vals)))

(defn- row->versioned-aggregate [row]
  {:aggregate (-> row (dissoc :version) sql->aggregate)
   :version   (-> row (get :version) sql->version)})

(defn- row->versioned-id [{:keys [id version]}]
  {:id      id
   :version (sql->version version)})

(deftype UserMapper []
  persistence.storage/Mapper
  (-lock [_ conn ids]
    (map row->versioned-id (user-locks conn {:ids ids})))
  (-select [_ conn ids]
    (map row->versioned-aggregate (user-select conn {:ids ids})))
  (-insert [_ conn aggregates]
    (user-insert conn {:vals (map aggregate->sql aggregates)}))
  (-delete [_ conn ids]
    (user-delete conn {:ids ids})))

(defn mapper []
  {User (UserMapper.)})
