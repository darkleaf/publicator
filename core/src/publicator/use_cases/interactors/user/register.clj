(ns publicator.use-cases.interactors.user.register
  (:require
   [publicator.use-cases.abstractions.storage :as storage]
   [publicator.use-cases.services.user-session :as user-session]
   [publicator.domain.aggregates.user :as user]
   [publicator.domain.aggregate :as agg]

   [datascript.core :as d]))

;; (defn- authorization-error []
;;   (cond
;;     (user-session/logged-in?) {:type ::already-logged-in}
;;     :else                     nil))

;; (defn- check-authorization! []
;;   (if-let [error (authorization-error)]
;;     (throw
;;      (ex-info "Authorization failed" error))))

;; (defn- check-params= [params]
;;   (if-let [exp (s/explain-data ::params params)]
;;     (e/left [::invalid-params exp])))

;; (defn- check-not-registered= [params]
;;   (if (user-q/get-by-login (:login params))
;;     (e/left [::already-registered])))

;; (def ^:private allowed-attrs
;;   #{:user/login :user-form/password})

(defn- create-user [tx-data]
  (let [user (-> (agg/build user/spec)
                 (agg/change tx-data
                             (agg/allow-attributes #{:user/login :user/password}))
                 (agg/change [[:db/add :root :user/state :active]]
                             agg/allow-everething))]
    (storage/transaction
     (storage/*create* user))
    user))

;; (defn can? []
;;   (nil? (authorization-error)))

(defn initial-params [])
  ;; (check-authorization!)
  ;; (-> (d/empty-db)
  ;;     (d/db-with [{:db/ident :root}])))

(defn process [tx-data]
;;   (check-authorization!)
;;   (check-not-registered! params)
  (let [user (create-user tx-data)]
    #_(user-session/log-in! user)
    user))
