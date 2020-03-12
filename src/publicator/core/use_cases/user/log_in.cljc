(ns publicator.core.use-cases.user.log-in
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.user :as user]
   [publicator.util :as u]
   [darkleaf.effect.core :refer [with-effects ! effect]]
   [darkleaf.effect.core-analogs :refer [->!]]
   [datascript.core :as d]))

(defn- authentication-validator [form]
  (u/<<-
   (with-effects)
   (if (agg/has-errors? form)
     form)
   (let [login    (d/q '[:find ?v . :where [:root :user/login ?v]] form)
         user     (! (effect [:persistence.user/get-by-login login]))
         error-tx [{:error/type   ::wrong-login-or-password
                    :error/entity :root}]])
   (if (nil? user)
     (d/db-with form error-tx))
   (if-not (user/active? user)
     (d/db-with form error-tx))
   (let [password (d/q '[:find ?v . :where [:root :user/password ?v]] form)
         digest   (d/q '[:find ?v . :where [:root :user/password-digest ?v]] user)
         correct? (! (effect [:hasher/check password digest]))])
   (if-not correct?
     (d/db-with form error-tx))
   form))

(defn validate-form [form]
  (with-effects
    (->! form
         (agg/validate)
         (agg/required-validator {:root [:user/login :user/password]})
         (authentication-validator))))

(def allowed-attributes #{:user/login :user/password})

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
      (loop [form (agg/allocate)]
        (let [tx-data (! (effect [:ui.form/edit form]))
              form    (->! form
                           (d/with tx-data)
                           (agg/check-extra-attrs! allowed-attributes)
                           :db-after
                           (validate-form))]
          (if (agg/has-errors? form)
            (recur form)
            (let [login (d/q '[:find ?v . :where [:root :user/login ?v]] form)
                  user  (! (effect [:persistence.user/get-by-login login]))
                  id    (d/q '[:find ?v . :where [:root :agg/id ?v]] user)]
              (! (effect [:session/assoc :current-user-id id]))
              (! (effect [:ui.screen/show :main])))))))))
