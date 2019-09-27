(ns publicator.use-cases.user.register
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.util :as u]))

(def allowed-attrs #{:user/login :user/password})

(defn- check-additional-attrs [datoms]
  (let [additional (->> datoms
                        (map :a)
                        (remove allowed-attrs)
                        (set))]
    (when (not-empty additional)
      [[:ui/show-additional-attributes-error additional]])))

(defn- fill-user-defaults [user]
  (agg/apply-tx user [{:db/ident   :root
                       :user/state :active
                       :user/role  :regular}]))

(defn- check-registration [user]
  (u/linearize
   (let [login (-> user agg/root :user/login)])
   [[:persistence/user-presence-by-login login] (fn [presence] <>)]
   (if presence
     [[:ui/show-main-screen]])
   [[:sub/return]]))

(defn- check-validation-errors [user]
  (let [errors (-> user agg/validate agg/errors)]
    (when (not-empty errors)
      [[:ui/show-validation-errors errors]])))

(defn- fill-id [user]
  (u/linearize
   [[:persistence/next-user-id] (fn [id] <>)]
   [[:sub/return (agg/apply-tx user [[:db/add :root :agg/id id]])]]))

(defn- fill-password-digest [user]
  (u/linearize
   [[:hasher/derive (-> user agg/root :user/password)] (fn [digest] <>)]
   [[:sub/return (agg/apply-tx user [[:db/add :root :user/password-digest digest]])]]))

(defn precondition [_]
  (u/linearize
   [[:session/get] (fn [session] <>)]
   (if (-> session :current-user-id some?)
     [[:sub/return [[:ui/show-main-screen]]]]
     [[:sub/return]])))

(defn process [tx-data]
  (u/linearize
   (u/sub precondition nil (fn [ex-effect] <>))
   (or ex-effect)
   (let [[user datoms] (-> :agg/user agg/allocate (agg/apply-tx* tx-data))])
   (or (check-additional-attrs datoms))
   (let [user (fill-user-defaults user)])
   (u/sub check-registration user (fn [_] <>))
   (u/sub fill-password-digest user (fn [user] <>))
   (or (check-validation-errors user))
   (u/sub fill-id user (fn [user] <>))
   (let [id (-> user agg/root :agg/id)])
   [[:do
     [:session/assoc :current-user-id id]
     [:persistence/save user]
     [:ui/show-main-screen]]]))
