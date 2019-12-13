(ns publicator.use-cases.user.register
  (:require
   [publicator.domain.aggregate :as agg]
   [darkleaf.effect.core :refer [eff !]]))

(def allowed-attrs #{:user/login :user/password})

(defn- check-additional-attrs [datoms]
  (eff
    (if-some [additional (->> datoms
                              (map :a)
                              (remove allowed-attrs)
                              (set)
                              (not-empty))]
      (! [:ui.error/show :additional-attributes additional]))))

(defn- fill-user-defaults [user]
  (agg/apply-tx user [{:db/ident   :root
                       :user/state :active
                       :user/role  :regular}]))

(defn- check-registration [user]
  (eff
    (let [login   (-> user agg/root :user/login)
          from-db (! [:persistence.user/get-by-login login])]
      (when from-db
        (! [:ui.screen/show :main])))))

(defn- check-validation-errors [user]
  (eff
    (if-some [errors (-> user agg/validate agg/errors not-empty)]
      (! [:ui.error/show :validation errors]))))

(defn- fill-id [user]
  (eff
    (let [id (! [:persistence/next-id :user])]
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
      [:ui.screen/show :main])))

(defn process []
  (eff
    (if-some [ex-effect (! (precondition))]
      (! ex-effect)
      (let [user          (-> :agg/user agg/allocate)
            tx-data       (! [:ui.form/edit user])
            [user datoms] (agg/apply-tx* user tx-data)
            _             (! (check-additional-attrs datoms))
            user          (! (fill-user-defaults user))
            _             (! (check-registration user))
            user          (! (fill-password-digest user))
            _             (! (check-validation-errors user))
            user          (! (fill-id user))
            id            (-> user agg/root :agg/id)]
        (! [:session/assoc :current-user-id id])
        (! [:persistence/save user])
        (! [:ui.screen/show :main])))))
