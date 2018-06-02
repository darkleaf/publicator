(ns publicator.persistence.id-generator
  (:require
   [jdbc.core :as jdbc]
   [publicator.domain.abstractions.id-generator :as id-generator]))

(deftype IdGenerator [data-source]
  id-generator/IdGenerator
  (-generate [_]
    (with-open [conn (jdbc/connection data-source)]
      (let [stmt (jdbc/prepared-statement conn "SELECT nextval('id-generator') AS id")
            resp (jdbc/fetch-one conn stmt)]
        (:id resp)))))

(defn binding-map [datasource]
  {#'id-generator/*id-generator* (->IdGenerator datasource)})
