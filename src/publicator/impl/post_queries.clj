(ns publicator.impl.post-queries
  (:require
   [hugsql.core :as hugsql]
   [jdbc.core :as jdbc]
   [publicator.interactors.abstractions.post-queries :as post-q]))

(hugsql/def-db-fns "publicator/impl/post_queries.sql")

(deftype GetList [data-source]
  post-q/GetList
  (-get-list [this]
    (with-open [conn (jdbc/connection data-source)]
      (post-get-list conn))))

(defn binding-map [data-source]
  {#'post-q/*get-list* (GetList. data-source)})
