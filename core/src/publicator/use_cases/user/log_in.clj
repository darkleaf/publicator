(ns publicator.use-cases.user.log-in
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.util :as u]))

(def allowed-attrs #{:user/login :user/password})

(defn- check-session [next]
  (u/linearize
   [[:get-session] (fn [session] <>)]
   (if (-> session :current-user-id some?)
     [[:show-screen :main]])
   (next)))

(defn- check-additional-attrs [datoms]
  (let [additional (->> datoms
                        (map :a)
                        (remove allowed-attrs)
                        (set))]
    (when (not-empty additional)
      [[:show-additional-attributes-error additional]])))

(defn- form-from-tx-data [tx-data]
  (let [report  (-> (agg/allocate :form.user/log-in)
                    (agg/with tx-data))
        form    (:db-after report)
        tx-data (:tx-data report)]
     [form tx-data]))

(defn- has-validation-errors [form]
  (let [errors (-> form agg/validate agg/errors)]
    (when (not-empty errors)
      [[:show-validation-errors errors]])))

(defn- fetch-user-by-login [login next]
  (u/linearize
   [[:get-user-by-login login]
    (fn [user] <>)]
   (if (nil? user)
     [[:show-user-not-found-error]])
   (next user)))

(defn- check-user-password [user password next]
  (u/linearize
   (let [digest (-> user agg/root :user/password-digest)])
   [[:check-password-digest digest password]
    (fn [ok] <>)]
   (if-not ok
     [[:show-user-not-found-error]])
   (next)))

(defn process [tx-data]
  (u/linearize
   (check-session (fn [] <>))
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
     [:assoc-session :current-user-id id]
     [:show-screen :main]]]))
