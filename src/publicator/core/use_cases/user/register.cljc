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
   (let [login   (agg/q user '[:find ?v . :where [:root :user/login ?v]])
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
           (agg/predicate-validator 'root
             {:form.user.register/password #".{8,255}"})
           (agg/required-validator 'root
             #{:form.user.register/password})
           (login-validator)))))

(md/decorate agg/allowed-attribute? :form.user/register
  (fn [super type attr]
    (or (super type attr)
        (#{:form.user.register/password} attr))))

(derive :form.user/register :agg.user/public)

(defn- fill-user-defaults [user]
  (agg/apply-tx user [{:db/ident   :root
                       :user/state :active
                       :user/role  :regular}]))

(defn- fill-id [user]
  (with-effects
    (let [id (! (effect [:persistence/next-id :user]))]
      (agg/apply-tx user [[:db/add :root :agg/id id]]))))

(defn- fill-password-digest [user form]
  (with-effects
    (let [password (agg/q form '[:find ?v . :where [:root :form.user.register/password ?v]])
          digest   (! (effect [:hasher/derive password]))]
      (agg/apply-tx user [[:db/add :root :user/password-digest digest]]))))

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
        (loop [form (agg/becomes user :form.user/register)]
          (let [tx-data (! (effect [:ui.form/edit form]))
                form    (->! form
                             (agg/apply-tx! tx-data)
                             (agg/validate))]
            (if (agg/has-errors? form)
              (recur form)
              (let [user (->! user
                              (agg/apply-tx tx-data)
                              (fill-user-defaults)
                              (fill-password-digest form)
                              (agg/validate)
                              (agg/check-errors)
                              (fill-id))
                    id   (-> user agg/root :agg/id)]
                (! (effect [:persistence/create user]))
                (! (effect [:session/update #'assoc :current-user-id id]))
                (! (effect [:ui.screen/show :main]))))))))))
