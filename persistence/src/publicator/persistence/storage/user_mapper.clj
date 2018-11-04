(ns publicator.persistence.storage.user-mapper
  (:require
   [hugsql.core :as hugsql]
   [publicator.domain.aggregates.user :as user]
   [publicator.persistence.storage :as persistence.storage])
  (:import
   [publicator.domain.aggregates.user User]))

(hugsql/def-db-fns "publicator/persistence/storage/user_mapper.sql")

(defn- row->versioned-aggregate [row]
  {:aggregate (-> row (dissoc :version) user/map->User)
   :version   (-> row (get :version))})

(deftype UserMapper []
  persistence.storage/Mapper
  (-lock [_ conn ids]
    (user-locks conn {:ids ids}))
  (-select [_ conn ids]
    (map row->versioned-aggregate (user-select conn {:ids ids})))
  (-insert [_ conn aggregates]
    (user-insert conn {:vals (map vals aggregates)}))
  (-delete [_ conn ids]
    (user-delete conn {:ids ids})))

(defn mapper []
  {User (UserMapper.)})
