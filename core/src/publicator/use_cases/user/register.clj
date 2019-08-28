(ns publicator.use-cases.user.register
  (:require
   [publicator.domain.aggregates.user :as user]
   [publicator.domain.aggregate :as agg]
   [publicator.util :refer [<<- or-some]]))

(def allowed-msgs #{:user/login :user/password})

(defn- already-logged-in [session]
  (when (-> session :current-user-id some?)
    {:reaction {:type :show-screen
                :name :main}}))

(defn- has-additional-messages [msgs]
  (let [additional (->> msgs
                        (map first)
                        (remove allowed-msgs)
                        (set))]
    (when (not-empty additional)
      {:reaction {:type :show-additional-messages-error
                  :msgs additional}})))

(defn- ->user [msgs password->digest]
  (-> user/new-blank
      (agg/with-msgs msgs)
      (agg/with-msgs [[:user/state :add :root :active]])
      (user/fill-password-digest password->digest)))

(defn- has-validation-errors [user]
  (let [errors (-> user agg/validate agg/errors)]
    (when (not-empty errors)
      {:reaction {:type   :show-validation-errors
                  :errors errors}})))

(defn- fill-id [user new-user-ids]
  (let [id (first new-user-ids)]
    (agg/with-msgs user [[:agg/id :add :root id]])))

(defn process [msgs session login->user-presence password->digest new-user-ids]
  (<<-
   (or-some (already-logged-in session))
   (or-some (has-additional-messages msgs))
   (let [user (->user msgs password->digest)])
   (or-some (has-validation-errors user))
   (let [user (fill-id user new-user-ids)
         id   (-> user agg/root :agg/id)])
   {:set-session (assoc session :current-user-id id)
    :persist     [user]
    :reaction    {:type :show-screen
                  :name :main}}))
