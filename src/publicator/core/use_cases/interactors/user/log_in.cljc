(ns publicator.core.use-cases.interactors.user.log-in
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.use-cases.aggregates.user :as user]
   [publicator.core.use-cases.services.user-session :as user-session]
   [publicator.core.use-cases.services.form :as form]
   [publicator.core.use-cases.contracts :as contracts]
   [publicator.utils :as u :refer [<<-]]
   [darkleaf.effect.core :refer [with-effects ! effect]]
   [darkleaf.effect.core-analogs :refer [->!]]
   [datascript.core :as d]))

(defn- get-user [form]
  (let [{:keys [user/login]} (d/entity form :root)]
    (effect :persistence.user/get-by-login login)))

(defn- correct-password? [user form]
  (let [{:keys [user/password-digest]} (d/entity user :root)
        {:keys [user/password]}        (d/entity form :root)]
    (effect :hasher/check password password-digest)))

(defn- auth-validator [form]
  (<<-
   (with-effects)
   (if (agg/has-errors? form)
     form)
   (let [user (! (get-user form))])
   (if (or (nil? user)
           (not (user/active? user))
           (not (! (correct-password? user form))))
     (d/db-with form [{:error/type   ::wrong-login-or-password
                       :error/entity :root}]))
   form))

(defn validate-form [form]
  (with-effects
    (->! form
         (agg/validate)
         (agg/required-validator {:root [:user/login :user/password]})
         (agg/permitted-attrs-validator #{:user/login :user/password})
         (auth-validator))))

(defn precondition []
  (with-effects
    (if (! (user-session/logged-in?))
      (effect ::->already-logged-in)
      :pass)))

(defn form []
  (with-effects
    (! (! (precondition)))
    (! (effect ::->form (agg/allocate)))))

(defn process [form]
  (with-effects
    (! (! (precondition)))
    (->! form
         (agg/remove-errors)
         (validate-form)
         (form/check-errors))
    (let [user (! (get-user form))]
      (! (user-session/log-in! user))
      (! (effect ::->processed)))))

(swap! contracts/registry merge
       {`form                 {:args (fn [] true)}
        `process              {:args (fn [form] (d/db? form))}
        ::->form              {:effect (fn [form] (d/db? form))}
        ::->already-logged-in {:effect (fn [] true)}
        ::->processed         {:effect (fn [] true)}})
;; TODO: тут еще могут быть ошибки формы, но на него нет теста
