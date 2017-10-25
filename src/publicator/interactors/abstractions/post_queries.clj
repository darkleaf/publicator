(ns publicator.interactors.abstractions.post-queries)

(defprotocol GetList
  (-get-list [this]))

(declare ^:dynamic *get-list*)

(defn get-list []
  (-get-list *get-list*))
