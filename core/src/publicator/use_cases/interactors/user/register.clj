(ns publicator.use-cases.interactors.user.register
  (:require
   [publicator.domain.aggregates.user :as user]
   [publicator.domain.aggregate :as agg]))

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
  (if-some [end (already-logged-in session)]
    end
    (if-some [end (has-additional-messages msgs)]
      end
      (let [user (->user msgs password->digest)]
        (if-some [end (has-validation-errors user)]
          end
          (let [user (fill-id user new-user-ids)]
            {:set-session (assoc session :current-user-id (-> user agg/root :agg/id))
             :persist     [user]
             :reaction    {:type :show-screen
                           :name :main}}))))))
