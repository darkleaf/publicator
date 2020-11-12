(ns publicator.core.use-cases.interactors.user.register
  (:require
   [darkleaf.effect.core :refer [effect]]
   [darkleaf.generator.core :refer [generator yield]]
   [datascript.core :as d]
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.use-cases.aggregates.user :as user]
   [publicator.core.use-cases.contracts :as contracts]
   [publicator.core.use-cases.services.form :as form]
   [publicator.core.use-cases.services.user-session :as user-session]))

(defn- login-validator* [agg]
  (generator
    (if (agg/has-errors? agg)
      agg
      (let [login (agg/val-in agg :root :user/login)]
        (if (yield (effect :persistence.user/exists-by-login login))
          (d/db-with agg [{:error/type   ::existed-login
                           :error/entity :root
                           :error/attr   :user/login
                           :error/value  login}])
          agg)))))

(defn- make-user* [form]
  (generator
    (let [login           (agg/val-in form :root :user/login)
          password        (agg/val-in form :root :user/password)
          password-digest (yield (effect :hasher/derive password))]
      (agg/build {:db/ident             :root
                  :user/login           login
                  :user/password-digest password-digest
                  :user/state           :active}))))

(defn validate-form* [form]
  (generator
    (-> form
        (agg/validate)
        (agg/required-validator {:root [:user/login :user/password]})
        (agg/permitted-attrs-validator #{:user/login :user/password})
        (-> login-validator* yield))))

(defn- create-user* [user]
  (effect :persistence.user/create user))

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
    (let [user (-> form
                   (-> make-user* yield)
                   (user/validate)
                   (agg/check-errors)
                   (-> create-user* yield))]
      (yield (user-session/log-in* user))
      (yield (effect ::->processed user)))))

(swap! contracts/registry merge
       {`form*                {:args   (fn [] true)
                               :return nil?}
        ::->form              {:effect (fn [form] (d/db? form))}
        `process*             {:args   (fn [form] (d/db? form))
                               :return nil?}
        ::->processed         {:effect (fn [user] (d/db? user))}
        ::->already-logged-in {:effect (fn [] true)}
        ::->invalid-form      {:effect (fn [form] (d/db? form))}})
