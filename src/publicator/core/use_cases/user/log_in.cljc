(ns publicator.core.use-cases.user.log-in
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.user :as user]
   [publicator.util :as u]
   [darkleaf.effect.core :refer [with-effects ! effect]]
   [darkleaf.effect.core-analogs :refer [->!]]
   [darkleaf.multidecorators :as md]))

(defn- authentication-validator [form]
  (u/<<-
   (with-effects)
   (if (agg/has-errors? form)
     form)
   (let [login    (agg/q form '[:find ?v . :where [:root :form.user.log-in/login ?v]])
         user     (! (effect [:persistence.user/get-by-login login]))
         error-tx [{:error/type   ::wrong-login-or-password
                    :error/entity :root}]])
   (if (nil? user)
     (agg/apply-tx form error-tx))
   (if-not (user/active? user)
     (agg/apply-tx form error-tx))
   (let [password (agg/q form '[:find ?v . :where [:root :form.user.log-in/password ?v]])
         digest   (agg/q user '[:find ?v . :where [:root :user/password-digest ?v]])
         correct? (! (effect [:hasher/check password digest]))])
   (if-not correct?
     (agg/apply-tx form error-tx))
   form))

(md/decorate agg/validate :form.user/log-in
  (fn [super agg]
    (with-effects
      (->! (super agg)
           (agg/predicate-validator 'root
             {:form.user.log-in/login    #"\w{3,255}"
              :form.user.log-in/password #".{8,255}"})
           (agg/required-validator  'root
             #{:form.user.log-in/login
               :form.user.log-in/password})
           (authentication-validator)))))

(md/decorate agg/allowed-attribute? :form.user/log-in
  (fn [super type attr]
    (or (super type attr)
        (#{:form.user.log-in/login
           :form.user.log-in/password} attr))))

(defn precondition []
  (with-effects
    (if (-> (! (effect [:session/get]))
            :current-user-id
            some?)
      (effect [:ui.screen/show :main]))))

(defn process []
  (with-effects
    (if-some [ex-effect (! (precondition))]
      (! ex-effect)
      (loop [form (agg/allocate :form.user/log-in)]
        (let [tx-data (! (effect [:ui.form/edit form]))
              form    (->! form
                           (agg/apply-tx! tx-data)
                           (agg/validate))]
          (if (agg/has-errors? form)
            (recur form)
            (let [login (agg/q form '[:find ?v . :where [:root :form.user.log-in/login ?v]])
                  user  (! (effect [:persistence.user/get-by-login login]))
                  id    (agg/q user '[:find ?v . :where [:root :agg/id ?v]])]
              (! (effect [:session/update #'assoc :current-user-id id]))
              (! (effect [:ui.screen/show :main])))))))))
