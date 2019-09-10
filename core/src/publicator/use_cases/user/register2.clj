(ns publicator.use-cases.user.register2
  (:require
   [publicator.domain.aggregates.user :as user]
   [publicator.domain.aggregate :as agg]
   [publicator.util :as u]))

(def allowed-msgs #{:user/login :user/password})

(defn- check-additional-messages [msgs]
  (let [additional (->> msgs
                        (map first)
                        (remove allowed-msgs)
                        (set))]
    (when (not-empty additional)
      [[:show-additional-messages-error additional]])))

(defn- check-session [next]
  (u/linearize
   [[:get-session] (fn [session] <>)]
   (if (-> session :current-user-id some?)
     [[:show-screen :main]])
   (next nil)))

(defn- user-from-msgs [msgs]
  (-> user/new-blank
      (agg/with-msgs msgs)
      (agg/with-msgs [[:user/state :add :root :active]])))

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
       (agg/with-msgs [[:agg/id :add :root id]])
       (next))))

(defn fill-password-digest [user next]
  (u/linearize
   [[:get-password-digest (-> user agg/root :user/password)] (fn [digest] <>)]
   (-> user
       (agg/with-msgs [[:user/password-digest :add :root digest]])
       (next))))

(defn process [msgs]
  (u/linearize
   (or (check-additional-messages msgs))
   (check-session (fn [_] <>))
   (let [user (user-from-msgs msgs)])
   (check-registration user (fn [_] <>))
   (fill-password-digest user (fn [user] <>))
   (or (check-validation-errors user))
   (fill-id user (fn [user] <>))
   [[:do
     [:assoc-session :current-user-id (-> user agg/root :agg/id)]
     [:persist user]
     [:show-screen :main]]]))
