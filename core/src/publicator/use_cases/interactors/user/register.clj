(ns publicator.use-cases.interactors.user.register
  (:require
   [publicator.use-cases.abstractions.storage :as storage]
   [publicator.use-cases.services.user-session :as user-session]
   [publicator.domain.aggregates.user :as user]
   [publicator.domain.aggregate :as agg]))

(defn authorization-error []
  (cond
    (user-session/logged-in?) {:type ::already-logged-in}))

(defn- check-authorization! []
  (if-let [error (authorization-error)]
    (throw
     (ex-info "Authorization failed" error))))

(defn- create-user [tx-data]
  (let [user (-> (agg/build user/spec)
                 (agg/change tx-data
                             (agg/allow-attributes #{:user/login :user/password}))
                 (agg/change [[:db/add :root :user/state :active]]
                             agg/allow-everething))]
    (storage/transaction
     (storage/*create* user))
    user))

(defn initial []
  (check-authorization!)
  {:schema (get user/spec :schema {})
   :tx     []})

(defn process [tx-data]
  (check-authorization!)
  (let [user (create-user tx-data)]
    (user-session/log-in! user)
    user))
