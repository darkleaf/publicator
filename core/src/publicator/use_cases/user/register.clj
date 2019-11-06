(ns publicator.use-cases.user.register
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.util :as u]
   [darkleaf.effect.core :refer [eff !]]))

(def allowed-attrs #{:user/login :user/password})

(defn- check-additional-attrs [datoms]
  (eff
    (if-let [additional (->> datoms
                             (map :a)
                             (remove allowed-attrs)
                             (set)
                             (not-empty))]
      (! [:ui/show-additional-attributes-error additional]))))

(defn- fill-user-defaults [user]
  (agg/apply-tx user [{:db/ident   :root
                       :user/state :active
                       :user/role  :regular}]))

(defn- check-registration [user]
  (eff
    (let [login    (-> user agg/root :user/login)
          presence (! [:persistence/user-presence-by-login login])]
      (when presence
        (! [:ui/show-main-screen])))))

(defn- check-validation-errors [user]
  (eff
    (if-let [errors (-> user agg/validate agg/errors not-empty)]
      (! [:ui/show-validation-errors errors]))))

(defn- fill-id [user]
  (eff
    (let [id (! [:persistence/next-user-id])]
      (agg/apply-tx user [[:db/add :root :agg/id id]]))))

(defn- fill-password-digest [user]
  (eff
    (let [password (-> user agg/root :user/password)
          digest   (! [:hasher/derive password])]
      (agg/apply-tx user [[:db/add :root :user/password-digest digest]]))))

(defn precondition []
  (eff
    (when (-> (! [:session/get])
              :current-user-id
              some?)
      [:ui/show-main-screen])))

(defn process [tx-data]
  (eff
    (if-some [ex-effect (! (precondition))]
      (! ex-effect)
      (let [[user datoms] (-> :agg/user agg/allocate (agg/apply-tx* tx-data))
            _             (! (check-additional-attrs datoms))
            user          (! (fill-user-defaults user))
            _             (! (check-registration user))
            user          (! (fill-password-digest user))
            _             (! (check-validation-errors user))
            user          (! (fill-id user))
            id            (-> user agg/root :agg/id)]
        (! [:session/assoc :current-user-id id])
        (! [:persistence/save user])
        (! [:ui/show-main-screen])))))
