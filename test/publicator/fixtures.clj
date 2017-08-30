(ns publicator.fixtures
  (:require
   [publicator.domain.utils.password :as password]
   [clojure.test :as t]))

(defn fake-password [f]
  (binding [password/*encrypt-fn* identity
            password/*check-fn* =]
    (f)))

(def all (t/join-fixtures [fake-password]))
