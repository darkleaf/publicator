(ns publicator.core.use-cases.interactors.user.update
  (:require
   [darkleaf.effect.core :refer [effect]]
   [darkleaf.generator.core :refer [generator yield]]
   [datascript.core :as d]
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.author :as author]
   [publicator.core.use-cases.aggregates.user :as user]
   [publicator.core.use-cases.contracts :as contracts]
   [publicator.core.use-cases.services.form :as form]
   [publicator.core.use-cases.services.user-session :as user-session]
   [publicator.utils :refer [<<-]]))

(defn ->readable-attr?* []
  (generator
    #{:user/login
      :user/state
      :user/admin?
      :user/author?
      :user/password
      :translation/root
      :translation/lang
      :author.translation/first-name
      :author.translation/last-name}))

(defn ->updatable-attr?* []
  (generator
    (let [current-user (yield (user-session/user*))]
      (cond-> #{:user/password
                :user/author?
                :translation/root
                :translation/lang
                :author.translation/first-name
                :author.translation/last-name}
        (user/admin? current-user) (conj :user/state :user/admin?)))))

(defn validate-form* [form]
  (generator
    (cond-> form
      :always             (agg/validate)
      :always             (agg/required-validator {:root [:user/login :user/state]})
      :always             (agg/permitted-attrs-validator (yield (->readable-attr?*)))
      (user/author? form) (author/validate))))

(defn- find-user* [id]
  (generator
    (if-some [user (yield (effect :persistence.user/get-by-id id))]
      user
      (yield (effect ::->user-not-found)))))

(defn- update-user* [user]
  (effect :persistence.user/update user))

(defn- update-password* [user]
  (generator
    (let [password        (-> user agg/root :user/password)
          password-digest (yield (effect :hasher/derive password))]
      (d/db-with user [[:db/add :root :user/password-digest password-digest]
                       [:db/retract :root :user/password password]]))))

(defn precondition** [user]
  (<<-
   (generator)
   (let [current-user   (yield (user-session/user*))
         not-authorized (effect ::->not-authorized)])
   (if (nil? user) not-authorized)
   (if (nil? current-user) not-authorized)
   (if (= current-user user) :pass)
   (if (user/admin? current-user) :pass)
   not-authorized))

(defn form* [id]
  (<<-
   (generator)
   (let [user (yield (find-user* id))])
   (do (yield (yield (precondition** user))))
   (let [form (agg/filter-datoms user (yield (->readable-attr?*)))])
   (yield (effect ::->form form))))

(defn process* [id form]
  (<<-
   (generator)
   (do (-> form
           (agg/remove-errors)
           (-> validate-form* yield)
           (-> form/check-errors* yield)))
   (let [user (yield (find-user* id))])
   (do (yield (yield (precondition** user))))
   (let [changes (form/changes user form (yield (->updatable-attr?*)))
         user    (-> user
                     (d/db-with changes)
                     (-> update-password* yield)
                     (user/validate)
                     (agg/check-errors)
                     (-> update-user* yield))])
   (yield (effect ::->processed user))))

(swap! contracts/registry merge
       {`form*             {:args   (fn [id] (pos-int? id))
                            :return nil?}
        `process*          {:args   (fn [id form] (and (pos-int? id) (d/db? form)))
                            :return nil?}
        ::->form           {:effect (fn [form] (d/db? form))}
        ::->processed      {:effect (fn [user] (d/db? user))}
        ::->not-authorized {:effect (fn [] true)}
        ::->user-not-found {:effect (fn [] true)}
        ::->invalid-form   {:effect (fn [form] (d/db? form))}})
