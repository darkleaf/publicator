(ns publicator.persistence.init
  (:require
   [publicator.persistence.types]
   [hugsql.core :as hugsql]
   [hugsql.adapter.clojure-jdbc :as cj-adapter]))

(hugsql/set-adapter! (cj-adapter/hugsql-adapter-clojure-jdbc))
