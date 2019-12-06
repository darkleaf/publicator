(ns publicator.use-cases.user.log-in
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.user :as user]
   [darkleaf.effect.core :refer [eff !]]
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

(defn- check-additional-attrs [datoms]
  (eff
    (if-some [additional (->> datoms
                              (map :a)
                              (remove allowed-attrs)
                              (set)
                              (not-empty))]
      (! [:ui/show-additional-attributes-error additional]))))

(defn- has-validation-errors [form]
  (eff
    (if-some [errors (-> form agg/validate agg/errors not-empty)]
      (! [:ui/show-validation-errors errors]))))

(defn- fetch-user-by-login [login]
  (eff
    (if-some [user (! [:persistence/user-by-login login])]
      user
      (! [:ui/show-user-not-found-error]))))

(defn- check-user-password [user password]
  (eff
    (let [digest (-> user agg/root :user/password-digest)
          ok?    (! [:hasher/check password digest])]
      (if-not ok?
        (! [:ui/show-user-not-found-error])))))

(defn precondition []
  (eff
    (when (-> (! [:session/get])
              :current-user-id
              some?)
      [:ui/show-main-screen])))

(defn process []
  (eff
    (if-some [ex-effect (! (precondition))]
      (! ex-effect)
      (let [form          (agg/allocate :form.user/log-in)
            tx-data       (! [:ui/edit form])
            [form datoms] (agg/apply-tx* form tx-data)
            _             (! (check-additional-attrs datoms))
            _             (! (has-validation-errors form))
            form-root     (agg/root form)
            login         (:user/login form-root)
            password      (:user/password form-root)
            user          (! (fetch-user-by-login login))
            _             (! (check-user-password user password))
            _             (if-not (user/active? user)
                            (! [:ui/show-main-screen]))
            id            (-> user agg/root :agg/id)]
        (! [:session/assoc :current-user-id id])
        (! [:ui/show-main-screen])))))
