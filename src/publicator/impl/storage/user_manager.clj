(ns publicator.impl.storage.user-manager
  (:require
   [hugsql.core :as hugsql]
   [publicator.domain.user :as user]
   [publicator.impl.storage :as impl.storage])
  (:import
   [publicator.domain.user User]))

(hugsql/def-db-fns "publicator/impl/storage/user_manager.sql")

(defn- get-version [row]
  (-> row :version .getValue))

(defn- row->box [row]
  (let [version (get-version row)
        row     (dissoc row :version)
        entity  (user/map->User row)]
     (impl.storage/build-box entity entity (:id entity) version)))

(defn- lock-row->map [row]
  (let [id      (:id row)
        version (get-version row)]
    {:id id, :version version}))

(deftype UserManager []
  impl.storage/Manager
  (-lock [_ conn ids]
    (map lock-row->map (user-locks conn {:ids ids})))
  (-select [_ conn ids]
    (map row->box (user-select conn {:ids ids})))
  (-insert [_ conn boxes]
    (user-insert conn {:vals (map #(-> % deref vals) boxes)}))
  (-delete [_ conn ids]
    (user-delete conn {:ids ids})))


(defn manager []
  {User (UserManager.)})
