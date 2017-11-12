(ns publicator.impl.id-generator
  (:require
   [jdbc.core :as jdbc]
   [publicator.domain.abstractions.id-generator :as id-generator]))

(defn- generate [conn]
  (let [stmt (jdbc/prepared-statement conn "SELECT nextval('id-generator') AS res")
        res  (jdbc/fetch-one conn stmt)]
    (:res res)))

(deftype IdGenerator [with-conn]
  id-generator/IdGenerator
  (-generate [_]
    (with-conn generate)))

(defn binding-map [with-conn]
  {#'id-generator/*id-generator* (IdGenerator. with-conn)})
