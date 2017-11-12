(ns publicator.impl.id-generator
  (:require
   [jdbc.core :as jdbc]
   [publicator.domain.abstractions.id-generator :as id-generator]))

(deftype IdGenerator [data-source]
  id-generator/IdGenerator
  (-generate [_]
    (with-open [conn (jdbc/connection data-source)]
      (let [res (jdbc/fetch-one conn "SELECT nextval('id-generator') AS res")]
        (:res res)))))

(defn binding-map [data-source]
  {#'id-generator/*id-generator* (IdGenerator. data-source)})
