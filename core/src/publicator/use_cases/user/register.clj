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
      [[:show-additional-attributes-error additional]])))

(defn- check-session [next]
  (u/linearize
   [[:get-session] (fn [session] <>)]
   (if (-> session :current-user-id some?)
     [[:show-screen :main]])
   (next nil)))

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
   [[:get-user-presence-by-login (-> user agg/root :user/login)] (fn [presence] <>)]
   (if presence
     [[:show-screen :main]])
   (next nil)))

(defn- check-validation-errors [user]
  (let [errors (-> user agg/validate agg/errors)]
    (when (not-empty errors)
      [[:show-validation-errors errors]])))

(defn- fill-id [user next]
  (u/linearize
   [[:get-new-user-id] (fn [id] <>)]
   (-> user
       (agg/agg-with [[:db/add :root :agg/id id]])
       (next))))

(defn fill-password-digest [user next]
  (u/linearize
   [[:get-password-digest (-> user agg/root :user/password)] (fn [digest] <>)]
   (-> user
       (agg/agg-with [[:db/add :root :user/password-digest digest]])
       (next))))

(defn process [tx-data]
  (u/linearize
   (let [[user datoms] (user-from-tx-data tx-data)])
   (or (check-additional-attrs datoms))
   (let [user (fill-user-defaults user)])
   (check-session (fn [_] <>))
   (check-registration user (fn [_] <>))
   (fill-password-digest user (fn [user] <>))
   (or (check-validation-errors user))
   (fill-id user (fn [user] <>))
   [[:do
     [:assoc-session :current-user-id (-> user agg/root :agg/id)]
     [:persist user]
     [:show-screen :main]]]))
