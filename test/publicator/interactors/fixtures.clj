(ns publicator.interactors.fixtures
  (:require
   [publicator.domain.utils.password :as password]
   [clojure.test :as t]))

(defn fake-password [f]
  (with-redefs-fn {#'password/encrypt identity
                   #'password/check =}
    f))

(def all (t/join-fixtures [fake-password]))
