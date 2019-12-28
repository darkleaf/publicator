(ns publicator.core.use-cases.user.register
  (:require
   [publicator.core.domain.aggregate :as agg]
   [darkleaf.multidecorators :as md]
   [darkleaf.effect.core :refer [eff ! effect]]


   [datascript.core :as d]))

(derive :form.user/register :agg/user)

(md/decorate agg/validate :form.user/register
  (fn [super agg]
    (-> (super agg)
        (agg/predicate-validator 'root
          {:user/password #".{8,255}"})
        (agg/required-validator 'root
          #{:user/password}))))

(def allowed-attrs #{:user/login :user/password})

(defn- filter-attrs [user]
  (let [datoms (->> (d/datoms user :eavt)
                    (filter (fn [{:keys [a]}]
                              (or (#{"db" "error"} (namespace a))
                                  (allowed-attrs a)))))]
    (-> user
        empty
        (agg/apply-tx datoms))))

(defn- check-additional-attrs [datoms]
  (eff
    (if-some [additional (->> datoms
                              (remove (fn [{:keys [a]}]
                                        (or (#{"db"} (namespace a))
                                            (allowed-attrs a))))
                              (set)
                              (not-empty))]
      (! (effect [:ui.error/show :additional-attributes additional])))))

(defn- fill-user-defaults [user]
  (agg/apply-tx user [{:db/ident   :root
                       :user/state :active
                       :user/role  :regular}]))

(defn- validate-uniq-login [user]
  (eff
    (let [login   (-> user agg/root :user/login)
          from-db (! (effect [:persistence.user/get-by-login login]))]
      (cond-> user
        (some? from-db)
        (agg/apply-tx [{:error/type   ::uniq-login
                        :error/entity :root
                        :error/attr   :user/login
                        :error/value  login}])))))

(defn- fill-id [user]
  (eff
    (let [id (! (effect [:persistence/next-id :user]))]
      (agg/apply-tx user [[:db/add :root :agg/id id]]))))

(defn- fill-password-digest [user]
  (eff
    (let [password (-> user agg/root :user/password)
          digest   (! (effect [:hasher/derive password]))]
      (agg/apply-tx user [[:db/add :root :user/password-digest digest]]))))

(defn precondition []
  (eff
    (when (-> (! (effect [:session/get]))
              :current-user-id
              some?)
      (effect [:ui.screen/show :main]))))

(defn process []
  (eff
    (if-some [ex-effect (! (precondition))]
      (! ex-effect)
      (let [initial (agg/allocate :form.user/register)]
        (loop [user initial]
          (let [form          (filter-attrs user)
                tx-data       (! (effect [:ui.form/edit form]))
                [user datoms] (agg/apply-tx* initial tx-data)
                _             (check-additional-attrs datoms)
                user          (fill-user-defaults user)
                user          (! (fill-password-digest user))
                user          (agg/validate user)
                user          (! (validate-uniq-login user))]
            (if (agg/has-errors? user)
              (recur user)
              (let [user (! (fill-id user))
                    id   (-> user agg/root :agg/id)]
                (! (effect [:persistence/save user]))
                (! (effect [:session/assoc :current-user-id id]))
                (! (effect [:ui.screen/show :main]))))))))))
