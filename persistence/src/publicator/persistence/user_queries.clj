(ns publicator.persistence.user-queries
  (:require
   [publicator.persistence.types]
   [hugsql.core :as hugsql]
   [hugsql.adapter.clojure-jdbc :as cj-adapter]
   [jdbc.core :as jdbc]
   [publicator.use-cases.abstractions.user-queries :as user-q]
   [publicator.domain.aggregates.user :as user]))

(hugsql/def-db-fns "publicator/persistence/user_queries.sql"
  {:adapter (cj-adapter/hugsql-adapter-clojure-jdbc)})

(deftype GetByLogin [data-source]
  user-q/GetByLogin
  (-get-by-login [this login]
    (with-open [conn (jdbc/connection data-source)]
      (when-let [row (user-get-by-login conn {:login login})]
        (user/map->User row)))))

(defn binding-map [data-source]
  [#'user-q/*get-by-login* (GetByLogin. data-source)])
