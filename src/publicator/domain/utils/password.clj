(ns publicator.domain.utils.password
  (:require
   [buddy.hashers :as hashers]))

(defn encrypt [password]
  (hashers/derive password))

(defn check [encrypted password]
  (hashers/check password encrypted))
