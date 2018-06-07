(ns publicator.persistence.user-queries
  (:require
   [hugsql.core :as hugsql]
   [hugsql.adapter.clojure-jdbc :as cj-adapter]
   [jdbc.core :as jdbc]
   [publicator.use-cases.abstractions.user-queries :as user-q]
   [publicator.domain.aggregates.user :as user]))

(hugsql/def-db-fns "publicator/persistence/user_queries.sql"
  {:adapter (cj-adapter/hugsql-adapter-clojure-jdbc)})

(defn- sql->user [raw]
  (-> raw
      (update :posts-ids #(-> % .getArray vec))
      (update :created-at #(.toInstant %))
      (user/map->User)))

(deftype GetByLogin [data-source]
  user-q/GetByLogin
  (-get-by-login [this login]
    (with-open [conn (jdbc/connection data-source)]
      (sql->user (user-get-by-login conn {:login login})))))

(defn binding-map [data-source]
  [#'user-q/*get-by-login* (GetByLogin. data-source)])
