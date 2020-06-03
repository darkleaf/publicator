(ns publicator.core.use-cases.interactors.user.update
  (:require
   [darkleaf.effect.core :refer [with-effects ! effect]]
   [darkleaf.effect.core-analogs :refer [->!]]
   [datascript.core :as d]
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.author :as author]
   [publicator.core.use-cases.aggregates.user :as user]
   [publicator.core.use-cases.contracts :as contracts]
   [publicator.core.use-cases.services.form :as form]
   [publicator.core.use-cases.services.user-session :as user-session]
   [publicator.utils :as u :refer [<<-]]))

(defn ->readable-attr? []
  (with-effects
    #{:user/login
      :user/state
      :user/admin?
      :user/author?
      :user/password
      :author.translation/author
      :author.translation/lang
      :author.translation/first-name
      :author.translation/last-name}))

(defn ->updatable-attr? []
  (with-effects
    (let [current-user (! (user-session/user))]
      (cond-> #{:user/password
                :user/author?
                :author.translation/author
                :author.translation/lang
                :author.translation/first-name
                :author.translation/last-name}
        (user/admin? current-user) (conj :user/state :user/admin?)))))

(defn validate-form [form]
  (with-effects
    (cond-> form
      :always             (agg/validate)
      :always             (agg/required-validator {:root [:user/login :user/state]})
      :always             (agg/permitted-attrs-validator (! (->readable-attr?)))
      (user/author? form) (author/validate))))

(defn- find-user [id]
  (with-effects
    (if-some [user (! (effect :persistence.user/get-by-id id))]
      user
      (! (effect ::->user-not-found)))))

(defn- update-user [user]
  (effect :persistence.user/update user))

(defn- update-password [user]
  (with-effects
    (let [password        (agg/val-in user :root :user/password)
          password-digest (! (effect :hasher/derive password))]
      (d/db-with user [[:db/add :root :user/password-digest password-digest]
                       [:db/retract :root :user/password password]]))))

(defn precondition [user]
  (<<-
   (with-effects)
   (let [current-user   (! (user-session/user))
         not-authorized (effect ::->not-authorized)])
   (if (nil? user) not-authorized)
   (if (nil? current-user) not-authorized)
   (if (= current-user user) :pass)
   (if (user/admin? current-user) :pass)
   not-authorized))

(defn form [id]
  (<<-
   (with-effects)
   (let [user (! (find-user id))])
   (do (! (! (precondition user))))
   (let [form (agg/filter-datoms user (! (->readable-attr?)))])
   (! (effect ::->form form))))

(defn process [id form]
  (<<-
   (with-effects)
   (do (->! form
            (agg/remove-errors)
            (validate-form)
            (form/check-errors)))
   (let [user (! (find-user id))])
   (do (! (! (precondition user))))
   (let [changes (form/changes user form (! (->updatable-attr?)))
         user    (->! user
                      (d/db-with changes)
                      (update-password)
                      (user/validate)
                      (agg/check-errors)
                      (update-user))])
   (! (effect ::->processed user))))

(swap! contracts/registry merge
       {`form              {:args (fn [id] (pos-int? id))}
        `process           {:args (fn [id form] (and (pos-int? id) (d/db? form)))}
        ::->form           {:effect (fn [form] (d/db? form))}
        ::->processed      {:effect (fn [user] (d/db? user))}
        ::->not-authorized {:effect (fn [] true)}
        ::->user-not-found {:effect (fn [] true)}
        ::->invalid-form   {:effect (fn [form] (d/db? form))}})
