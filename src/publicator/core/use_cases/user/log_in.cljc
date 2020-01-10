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
   (let [login    (agg/q form '[:find ?v . :where [:root :user/login ?v]])
         user     (! (effect [:persistence.user/get-by-login login]))
         error-tx [{:error/type   ::wrong-login-or-password
                    :error/entity :root}]])
   (if (nil? user)
     (agg/apply-tx form error-tx))
   (if-not (user/active? user)
     (agg/apply-tx form error-tx))
   (let [password (agg/q form '[:find ?v . :where [:root :user/password ?v]])
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
             {:user/login    #"\w{3,255}"
              :user/password #".{8,255}"})
           (agg/required-validator  'root
             #{:user/login
               :user/password})
           (authentication-validator)))))

(defn- allowed-datom? [{:keys [a]}]
  (or (#{"db" "error"} (namespace a))
      (#{:user/login :user/password} a)))

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
                           (update-form tx-data)
                           (agg/validate))]
          (if (agg/has-errors? form)
            (recur form)
            (let [login (agg/q form '[:find ?v . :where [:root :user/login ?v]])
                  user  (! (effect [:persistence.user/get-by-login login]))
                  id    (agg/q user '[:find ?v . :where [:root :agg/id ?v]])]
              (! (effect [:session/update #'assoc :current-user-id id]))
              (! (effect [:ui.screen/show :main])))))))))
