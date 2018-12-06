(ns publicator.persistence.post-queries
  (:require
   [hugsql.core :as hugsql]
   [jdbc.core :as jdbc]
   [publicator.use-cases.abstractions.post-queries :as post-q]
   [publicator.domain.aggregates.post :as post]))

(hugsql/def-db-fns "publicator/persistence/post_queries.sql")

(defn- sql->post [row]
  (post/map->Post row))

(deftype GetList [data-source]
  post-q/GetList
  (-get-list [this]
    (with-open [conn (jdbc/connection data-source)]
      (map sql->post (post-get-list conn)))))

(deftype GetById [data-source]
  post-q/GetById
  (-get-by-id [this id]
    (with-open [conn (jdbc/connection data-source)]
      (when-let [row (post-get-by-id conn {:id id})]
        (sql->post row)))))

(defn binding-map [data-source]
  {#'post-q/*get-list*  (GetList. data-source)
   #'post-q/*get-by-id* (GetById. data-source)})
