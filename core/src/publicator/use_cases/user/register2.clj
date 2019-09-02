(ns publicator.use-cases.user.register2
  (:require
   [publicator.domain.aggregates.user :as user]
   [publicator.domain.aggregate :as agg]
   [publicator.util :refer [linearize or-some]]))

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

(defn- ->user [msgs]
  (-> user/new-blank
      (agg/with-msgs msgs)
      (agg/with-msgs [[:user/state :add :root :active]])))


(defn- has-validation-errors [user]
  (let [errors (-> user agg/validate agg/errors)]
    (when (not-empty errors)
      {:reaction {:type   :show-validation-errors
                  :errors errors}})))

(defn- fill-id [user id]
  (agg/with-msgs user [[:agg/id :add :root id]]))

(defn fill-password-digest [user digest]
  (agg/with-msgs user [[:user/password-digest :add :root digest]]))

(defn process [msgs]
  (linearize
   (or-some (has-additional-messages msgs))
   {:get-session {:callback (fn [session] <>)}}
   (or-some (already-logged-in session))
   (let [user (->user msgs)])
   {:get-password-digest {:password (-> user agg/root :user/password)
                          :callback (fn bind-password-digest [digest] <>)}}
   (let [user (fill-password-digest user digest)])
   (or-some (has-validation-errors user))
   {:get-user-id {:callback (fn bind-user-id [id] <>)}}
   (let [user (fill-id user id)])
   {:set-session (assoc session :current-user-id id)
    :persist     [user]
    :reaction    {:type :show-screen
                  :name :main}}))
