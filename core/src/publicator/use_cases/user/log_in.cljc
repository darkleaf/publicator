(ns publicator.use-cases.user.log-in
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.util :as u]
   [darkleaf.multidecorators :as md]))

(md/decorate agg/validate :form.user/log-in
  (fn [super agg]
    (-> (super agg)
        (agg/predicate-validator 'root
          {:user/login    #"\w{3,255}"
           :user/password #".{8,255}"})
        (agg/required-validator  'root
          #{:user/login
            :user/password}))))

(def allowed-attrs #{:user/login :user/password})

(defn- check-session [next]
  (u/linearize
   [[:session/get] (fn [session] <>)]
   (if (-> session :current-user-id some?)
     [[:ui/show-main-screen]])
   (next)))

(defn- check-additional-attrs [datoms]
  (let [additional (->> datoms
                        (map :a)
                        (remove allowed-attrs)
                        (set))]
    (when (not-empty additional)
      [[:ui/show-additional-attributes-error additional]])))

(defn- form-from-tx-data [tx-data]
  (let [report  (-> (agg/allocate :form.user/log-in)
                    (agg/with tx-data))
        form    (:db-after report)
        tx-data (:tx-data report)]
     [form tx-data]))

(defn- has-validation-errors [form]
  (let [errors (-> form agg/validate agg/errors)]
    (when (not-empty errors)
      [[:ui/show-validation-errors errors]])))

(defn- fetch-user-by-login [login next]
  (u/linearize
   [[:persistence/user-by-login login]
    (fn [user] <>)]
   (if (nil? user)
     [[:ui/show-user-not-found-error]])
   (next user)))

(defn- check-user-password [user password next]
  (u/linearize
   (let [digest (-> user agg/root :user/password-digest)])
   [[:hasher/check password digest]
    (fn [ok] <>)]
   (if-not ok
     [[:ui/show-user-not-found-error]])
   (next)))

(defn check-env [next]
  (check-session next))

(defn process [tx-data]
  (u/linearize
   (check-env (fn [] <>))
   (let [[form datoms] (form-from-tx-data tx-data)])
   (or (check-additional-attrs datoms))
   (or (has-validation-errors form))
   (let [form-root (agg/root form)
         login     (:user/login form-root)
         password  (:user/password form-root)])
   (fetch-user-by-login login (fn [user] <>))
   (check-user-password user password (fn [] <>))
   (let [id (-> user agg/root :agg/id)])
   [[:do
     [:session/assoc :current-user-id id]
     [:ui/show-main-screen]]]))
