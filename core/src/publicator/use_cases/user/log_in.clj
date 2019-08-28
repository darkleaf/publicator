(ns publicator.use-cases.user.log-in
  (:require
   [publicator.domain.aggregates.user :as user]
   [publicator.use-cases.user.log-in-form :as form]
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

(defn- ->form [msgs]
  (-> form/blank
      (agg/with-msgs msgs)))

(defn- has-validation-errors [form]
  (let [errors (-> form agg/validate agg/errors)]
    (when (not-empty errors)
      {:reaction {:type   :show-validation-errors
                  :errors errors}})))

(defn- not-found [user]
  (when (nil? user)
    {:reaction {:type :show-not-found-error}}))

(defn- wrong-password [user password check-password]
  (let [digest (-> user agg/root :user/password-digest)]
    (if-not (check-password password digest)
      {:reaction {:type :show-wrong-password-error}})))

(defn process [msgs session login->user check-password]
  (<<-
   (or-some (already-logged-in session))
   (or-some (has-additional-messages msgs))
   (let [form (->form msgs)])
   (or-some (has-validation-errors form))
   (let [form-root (agg/root form)
         login     (:user/login form-root)
         password  (:user/password form-root)
         user      (login->user login)])
   (or-some (not-found user))
   (or-some (wrong-password user password check-password))
   (let [id (-> user agg/root :agg/id)])
   {:set-session (assoc session :current-user-id id)
    :reaction    {:type :show-screen
                  :name :main}}))
