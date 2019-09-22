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

(defn- check-session [next]
  (u/linearize
   [[:session/get] (fn [session] <>)]
   (if (-> session :current-user-id some?)
     [[:ui/show-main-screen]])
   (next)))

(defn- user-from-tx-data [tx-data]
  (let [report  (-> (agg/allocate :agg/user)
                    (agg/with tx-data))
        user    (:db-after report)
        tx-data (:tx-data report)]
    [user tx-data]))

(defn- fill-user-defaults [user]
  (agg/agg-with user [[:db/add :root :user/state :active]]))

(defn- check-registration [user next]
  (u/linearize
   (let [login (-> user agg/root :user/login)])
   [[:persistence/user-presence-by-login login] (fn [presence] <>)]
   (if presence
     [[:ui/show-main-screen]])
   (next)))

(defn- check-validation-errors [user]
  (let [errors (-> user agg/validate agg/errors)]
    (when (not-empty errors)
      [[:ui/show-validation-errors errors]])))

(defn- fill-id [user next]
  (u/linearize
   [[:persistence/next-user-id] (fn [id] <>)]
   (-> user
       (agg/agg-with [[:db/add :root :agg/id id]])
       (next))))

(defn- fill-password-digest [user next]
  (u/linearize
   [[:hasher/derive (-> user agg/root :user/password)] (fn [digest] <>)]
   (-> user
       (agg/agg-with [[:db/add :root :user/password-digest digest]])
       (next))))

(defn check-env [next]
  (check-session next))

(defn process [tx-data]
  (u/linearize
   (check-env (fn [] <>))
   (let [[user datoms] (user-from-tx-data tx-data)])
   (or (check-additional-attrs datoms))
   (let [user (fill-user-defaults user)])
   (check-registration user (fn [] <>))
   (fill-password-digest user (fn [user] <>))
   (or (check-validation-errors user))
   (fill-id user (fn [user] <>))
   (let [id (-> user agg/root :agg/id)])
   [[:do
     [:session/assoc :current-user-id id]
     [:persistence/save user]
     [:ui/show-main-screen]]]))
