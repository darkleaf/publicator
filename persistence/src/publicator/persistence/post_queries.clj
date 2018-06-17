(ns publicator.persistence.post-queries
  (:require
   [hugsql.core :as hugsql]
   [hugsql.adapter.clojure-jdbc :as cj-adapter]
   [jdbc.core :as jdbc]
   [publicator.use-cases.abstractions.post-queries :as post-q]
   [publicator.domain.aggregates.post :as post]
   [publicator.domain.aggregates.user :as user]))

(hugsql/def-db-fns "publicator/persistence/post_queries.sql"
  {:adapter (cj-adapter/hugsql-adapter-clojure-jdbc)})

(defn- sql->post [raw]
  (when raw
    (let [id        (:user-id raw)
          full-name (:user-full-name raw)]
      (-> raw
          (dissoc :user-id :user-full-name)
          (assoc ::user/id id, ::user/full-name full-name)
          (update :created-at #(.toInstant %))
          (post/map->Post)))))

(deftype GetList [data-source]
  post-q/GetList
  (-get-list [this]
    (with-open [conn (jdbc/connection data-source)]
      (map sql->post (post-get-list conn)))))

(deftype GetById [data-source]
  post-q/GetById
  (-get-by-id [this id]
    (with-open [conn (jdbc/connection data-source)]
      (sql->post (post-get-by-id conn {:id id})))))

(defn binding-map [data-source]
  {#'post-q/*get-list*  (GetList. data-source)
   #'post-q/*get-by-id* (GetById. data-source)})
