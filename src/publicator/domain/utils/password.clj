(ns publicator.domain.utils.password
  (:require
   [buddy.hashers :as hashers]))

(def ^:dynamic *encrypt-fn* hashers/derive)
(defn encrypt [password] (*encrypt-fn* password))

(def ^:dynamic *check-fn* hashers/check)
(defn check [encrypted password] (*check-fn* encrypted password))
