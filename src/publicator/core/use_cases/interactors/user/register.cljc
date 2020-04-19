(ns publicator.core.use-cases.interactors.user.register
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.user :as user]
   [publicator.core.use-cases.services.user-session :as user-session]
   [publicator.core.use-cases.services.form :as form]
   [publicator.utils :as u :refer [<<-]]
   [darkleaf.effect.core :refer [with-effects ! effect]]
   [darkleaf.effect.core-analogs :refer [->!]]
   [datascript.core :as d]))

(defn- login-validator [agg]
  (<<-
   (with-effects)
   (if (agg/has-errors? agg)
     agg)
   (let [{:keys [user/login]} (d/entity agg :root)])
   (if (! (effect [:persistence.user/exists-by-login login]))
     (d/db-with agg [{:error/type   ::existed-login
                      :error/entity :root
                      :error/attr   :user/login
                      :error/value  login}]))
   agg))

(defn- make-user [form]
  (with-effects
    (let [{:keys [user/login
                  user/password]} (d/entity form :root)
          password-digest         (! (effect [:hasher/derive password]))]
      (agg/allocate {:db/ident             :root
                     :user/login           login
                     :user/password-digest password-digest
                     :user/state           :active
                     :user/role            :regular}))))

(defn validate-form [form]
  (with-effects
    (->! form
         (agg/validate)
         (agg/required-validator {:root [:user/login :user/password]})
         (agg/permitted-attrs-validator #{:user/login :user/password})
         (login-validator))))

(defn- create-user [user]
  (effect [:persistence.user/create user]))

(defn precondition []
  (with-effects
    (if (! (user-session/logged-in?))
      (effect [::->already-logged-in])
      :pass)))

(defn form []
  (with-effects
    (! (! (precondition)))
    (! (effect [::->form (agg/allocate)]))))

(defn process [form]
  (with-effects
    (! (! (precondition)))
    (->! form
         (validate-form)
         (form/check-errors))
    (let [user (->! form
                    (make-user)
                    (user/validate)
                    (agg/check-errors!)
                    (create-user))]
      (! (user-session/log-in! user))
      (effect [::->processed user]))))
