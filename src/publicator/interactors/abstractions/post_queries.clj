(ns publicator.interactors.abstractions.post-queries
  (:require
   [publicator.interactors.projections.post-list :as post-list]
   [clojure.spec.alpha :as s]))

(defprotocol GetList
  (-get-list [this]))

(declare ^:dynamic *get-list*)

(defn get-list []
  {:post [(s/assert ::post-list/list %)]}
  (-get-list *get-list*))
