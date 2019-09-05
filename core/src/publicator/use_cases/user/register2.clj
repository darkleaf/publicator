(ns publicator.use-cases.user.register2
  (:require
   [publicator.domain.aggregates.user :as user]
   [publicator.domain.aggregate :as agg]
   [publicator.util :refer [linearize or-some]]))

(def allowed-msgs #{:user/login :user/password})

(defn- has-additional-messages [msgs]
  (let [additional (->> msgs
                        (map first)
                        (remove allowed-msgs)
                        (set))]
    (when (not-empty additional)
      [[:show-additional-messages-error additional]])))

(defn- already-logged-in [session]
  (when (-> session :current-user-id some?)
    [[:show-screen :main]]))

(defn- ->user [msgs]
  (-> user/new-blank
      (agg/with-msgs msgs)
      (agg/with-msgs [[:user/state :add :root :active]])))

(defn- already-registered [presence]
  (when presence
    [[:show-screen :main]]))

(defn- has-validation-errors [user]
  (let [errors (-> user agg/validate agg/errors)]
    (when (not-empty errors)
      [[:show-validation-errors errors]])))

(defn- fill-id [user id]
  (agg/with-msgs user [[:agg/id :add :root id]]))

(defn fill-password-digest [user digest]
  (agg/with-msgs user [[:user/password-digest :add :root digest]]))

(defn process [msgs]
  (linearize
   (or-some (has-additional-messages msgs))
   [[:get-session (fn [session] <>)]]
   (or-some (already-logged-in session))
   (let [user (->user msgs)])
   [[:get-user-presence-by-login (-> user agg/root :user/login) (fn [presence] <>)]]
   (or-some (already-registered presence))
   [[:get-password-digest (-> user agg/root :user/password) (fn [digest] <>)]]
   (let [user (fill-password-digest user digest)])
   (or-some (has-validation-errors user))
   [[:get-new-user-id (fn [id] <>)]]
   (let [user (fill-id user id)])
   [[:set-session (assoc session :current-user-id id)]
    [:persist user]
    [:show-screen :main]]))
