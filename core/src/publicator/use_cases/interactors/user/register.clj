(ns publicator.use-cases.interactors.user.register
  (:require
   [publicator.use-cases.abstractions.storage :as storage]
   [publicator.use-cases.abstractions.password-hasher :as password-hasher]
   [publicator.use-cases.services.user-session :as user-session]
   [publicator.domain.aggregates.user :as user]
   [datascript.core :as d]
   [publicator.domain.aggregate :as aggregate]))

;; (s/def ::params (utils.spec/only-keys :req-un [::user/login
;;                                                ::user/full-name
;;                                                ::user/password]))

(defn- authorization-error []
  (cond
    (user-session/logged-in?) {:type ::already-logged-in}
    :else                     nil))

(defn- check-authorization! []
  (if-let [error (authorization-error)]
    (throw
     (ex-info "Authorization failed" error))))

;; (defn- check-params= [params]
;;   (if-let [exp (s/explain-data ::params params)]
;;     (e/left [::invalid-params exp])))

;; (defn- check-not-registered= [params]
;;   (if (user-q/get-by-login (:login params))
;;     (e/left [::already-registered])))

(def ^:private allowed-attrs
  #{:user/login :user-form/password})

(defn- create-user [tx-data]
  ;; нужно накатить и проверить, что лишние атрибуты не появились.

  (let [tx-data (concat tx-data [[:db/add :root :user/state :active]
                                 [:db.fn/call
                                  (fn [db]
                                    (let [password        (-> db aggregate/root :user-form/password)
                                          password-digest (password-hasher/*derive* password)]
                                      [[:db/add :root :user/password-digest password-digest]
                                       [:db/retract :root :user-form/password password]]))]])

        user (user/build tx-data)]
    (storage/transaction
     (storage/*create* user))
    user))

;; (defn can? []
;;   (nil? (authorization-error)))

(defn initial-params []
  (check-authorization!)
  (-> (d/empty-db)
      (d/db-with [{:db/ident :root}])))

(defn process [tx-data]
;;   (check-authorization!)
;;   (check-not-registered! params)
  (let [user (create-user tx-data)]
    (user-session/log-in! user)
    user))


      ;; (validation/types [:user/login string?]
      ;;                   [:user/password-digest string?]
      ;;                   [:user/state +states+])))
