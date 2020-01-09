(ns publicator.core.use-cases.user.register
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.util :as u]
   [darkleaf.multidecorators :as md]
   [darkleaf.effect.core :refer [with-effects ! effect]]
   [darkleaf.effect.core-analogs :refer [->!]]))

(defn- login-validator [user]
  (u/<<-
   (with-effects)
   (if (agg/has-errors? user)
     user)
   (let [login   (-> user agg/root :user/login)
         exists? (! (effect [:persistence.user/exists-by-login login]))])
   (if exists?
     (agg/apply-tx user [{:error/type   ::existed-login
                          :error/entity :root
                          :error/attr   :user/login
                          :error/value  login}]))
   user))

(md/decorate agg/validate :form.user/register
  (fn [super agg]
    (with-effects
      (->! (super agg)
           (agg/required-validator 'root
             #{:user/password})
           (login-validator)))))

(derive :form.user/register :agg.user/public)

(defn- allowed-datom? [{:keys [a]}]
  (or (#{"db" "error"} (namespace a))
      (#{:user/login :user/password} a)))

(defn- user->form [user]
  (let [datoms (->> user
                    (agg/datoms)
                    (filter allowed-datom?))]
    (-> (agg/allocate :form.user/register)
        (agg/apply-tx datoms))))

(defn- fill-user-defaults [user]
  (agg/apply-tx user [{:db/ident   :root
                       :user/state :active
                       :user/role  :regular}]))

(defn- fill-id [user]
  (with-effects
    (let [id (! (effect [:persistence/next-id :user]))]
      (agg/apply-tx user [[:db/add :root :agg/id id]]))))

(defn- fill-password-digest [user]
  (with-effects
    (let [password (-> user agg/root :user/password)
          digest   (! (effect [:hasher/derive password]))]
      (agg/apply-tx user [[:db/add :root :user/password-digest digest]]))))

(defn- check-additional-attrs [datoms]
  (if-some [additional (->> datoms
                            (remove allowed-datom?)
                            (not-empty))]
    (throw (ex-info "Additional datoms" {:additional additional}))))

(defn- update-form [form tx-data]
  (let [[form datoms] (agg/apply-tx* form tx-data)]
    (check-additional-attrs datoms)
    form))

(defn precondition []
  (with-effects
    (when (-> (! (effect [:session/get]))
              :current-user-id
              some?)
      (effect [:ui.screen/show :main]))))

(defn process []
  (with-effects
    (if-some [ex-effect (! (precondition))]
      (! ex-effect)
      (let [user (agg/allocate :agg/user)]
        (loop [form (user->form user)]
          (let [tx-data (! (effect [:ui.form/edit form]))
                form    (->! form
                             (update-form tx-data)
                             (agg/validate))]
            (if (agg/has-errors? form)
              (recur form)
              (let [user (->! user
                              (agg/apply-tx tx-data)
                              (fill-user-defaults)
                              (fill-password-digest)
                              (agg/validate)
                              (agg/check-errors)
                              (fill-id))
                    id   (-> user agg/root :agg/id)]
                (! (effect [:persistence/save user]))
                (! (effect [:session/assoc :current-user-id id]))
                (! (effect [:ui.screen/show :main]))))))))))
