(ns publicator.core.use-cases.interactors.user.log-in
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.use-cases.aggregates.user :as user]
   [publicator.core.use-cases.services.user-session :as user-session]
   [publicator.core.use-cases.services.form :as form]
   [publicator.core.use-cases.contracts :as contracts]
   [publicator.utils :refer [<<-]]
   [darkleaf.generator.core :refer [generator yield]]
   [darkleaf.effect.core :refer [effect]]
   [datascript.core :as d]))

(defn- get-user* [form]
  (let [login (-> form agg/root :user/login)]
    (effect :persistence.user/get-by-login login)))

(defn- correct-password?* [user form]
  (let [password        (-> form agg/root :user/password)
        password-digest (-> user agg/root :user/password-digest)]
    (effect :hasher/check password password-digest)))

(defn- auth-validator* [form]
  (<<-
   (generator)
   (if (agg/has-errors? form)
     form)
   (let [user (yield (get-user* form))])
   (if (or (nil? user)
           (not (user/active? user))
           (not (yield (correct-password?* user form))))
     (d/db-with form [{:error/type   ::wrong-login-or-password
                       :error/entity :root}]))
   form))

(defn validate-form* [form]
  (generator
    (-> form
        (agg/validate)
        (agg/required-attrs-validator {:root [:user/login :user/password]})
        (agg/permitted-attrs-validator #{:user/login :user/password})
        (-> auth-validator* yield))))

(defn precondition** []
  (generator
    (if (yield (user-session/logged-in?*))
      (effect ::->already-logged-in)
      :pass)))

(defn form* []
  (generator
    (yield (yield (precondition**)))
    (yield (effect ::->form (agg/build)))))

(defn process* [form]
  (generator
    (yield (yield (precondition**)))
    (-> form
        (agg/remove-errors)
        (-> validate-form* yield)
        (-> form/check-errors* yield))
    (let [user (yield (get-user* form))]
      (yield (user-session/log-in* user))
      (yield (effect ::->processed)))))

(swap! contracts/registry merge
       {`form*                {:args   (fn [] true)
                               :return nil?}
        `process*             {:args   (fn [form] (d/db? form))
                               :return nil?}
        ::->form              {:effect (fn [form] (d/db? form))}
        ::->already-logged-in {:effect (fn [] true)}
        ::->processed         {:effect (fn [] true)}
        ::->invalid-form      {:effect (fn [form] (d/db? form))}})
