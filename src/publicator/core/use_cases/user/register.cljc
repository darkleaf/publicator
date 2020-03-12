(ns publicator.core.use-cases.user.register
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.user :as user]
   [publicator.util :as u]
   [darkleaf.effect.core :refer [with-effects ! effect]]
   [darkleaf.effect.core-analogs :refer [->!]]
   [datascript.core :as d]))

(defn- login-validator [agg]
  (u/<<-
   (with-effects)
   (if (agg/has-errors? agg)
     agg)
   (let [login   (d/q '[:find ?v . :where [:root :user/login ?v]] agg)
         exists? (! (effect [:persistence.user/exists-by-login login]))])
   (if exists?
     (d/db-with agg [{:error/type   ::existed-login
                      :error/entity :root
                      :error/attr   :user/login
                      :error/value  login}]))
   agg))

(swap! agg/schema merge
       {:form.user.register/password {:agg/predicate #".{8,255}"}})

(defn validate-form [form]
  (with-effects
    (->! form
         (agg/validate)
         (agg/required-validator {:root [:user/login
                                         :form.user.register/password]})
         (login-validator))))


(defn- fill-defaults [user]
  (d/db-with user [{:db/ident   :root
                    :user/state :active
                    :user/role  :regular}]))

(defn- fill-id [user]
  (with-effects
    (let [id (! (effect [:persistence.user/next-id]))]
      (d/db-with user [[:db/add :root :agg/id id]]))))

(defn- fill-password-digest [user]
  (with-effects
    (let [password (d/q '[:find ?v . :where [:root :form.user.register/password ?v]] user)
          digest   (! (effect [:hasher/derive password]))]
      (d/db-with user [[:db/add :root :user/password-digest digest]
                       [:db/retract :root :form.user.register/password password]]))))

(def allowed-attrs #{:user/login :form.user.register/password})

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
      (loop [form (agg/allocate)]
        (let [tx-data (! (effect [:ui.form/edit form]))
              form    (->! form
                           (d/with tx-data)
                           (agg/check-extra-attrs! allowed-attrs)
                           :db-after
                           (validate-form))]
          (if (agg/has-errors? form)
            (recur form)
            (let [user (->! (agg/allocate)
                            (d/db-with tx-data)
                            (fill-password-digest)
                            (fill-defaults)
                            (agg/validate)
                            (agg/check-errors!)
                            (fill-id))
                  id   (d/q '[:find ?v . :where [:root :agg/id ?v]] user)]
              (! (effect [:persistence.user/create user]))
              (! (effect [:session/assoc :current-user-id id]))
              (! (effect [:ui.screen/show :main])))))))))
