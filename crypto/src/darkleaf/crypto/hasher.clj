(ns darkleaf.crypto.hasher
  (:refer-clojure :exclude [derive])
  (:require
   [buddy.hashers :as h]))

(defn derive [[_ password]]
  (h/derive password))

(defn check [[_ attempt encrypted]]
  (h/check attempt encrypted))
