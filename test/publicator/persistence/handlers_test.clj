(ns publicator.persistence.handlers-test
  (:require
   [clojure.test :as t]
   [com.stuartsierra.component :as component]
   [darkleaf.effect.core :as e :refer [! effect with-effects]]
   [darkleaf.effect.middleware.context :as context]
   [darkleaf.effect.middleware.contract :as contract]
   [datascript.core :as d]
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.use-cases.contracts :as contracts]
   [publicator.persistence.components :as components]
   [publicator.persistence.handlers :as sut]))

(defmacro with-system [binding & body]
  {:pre [(vector? binding)
         (= 2 (count binding))]}
  (let [system-name (first binding)]
    `(let [~@binding
           ~system-name (component/start-system ~system-name)]
       (try
         ~@body
         (finally
           (component/stop-system ~system-name))))))

(defn- test-system []
  (component/system-map
   :sourceable "jdbc:pgsql://localhost:3402/postgres?user=postgres&password=password"
   :transactable (component/using (components/test-transactable) {:connectable :sourceable})
   :migration (component/using (components/migration) [:sourceable])))

(defn- run [ef & args]
  (with-system [system (test-system)]
    (let [contracts    (merge @contracts/registry
                              {'my/ef {:args   (constantly true)
                                       :return (constantly true)}})
          continuation (-> ef
                           (e/continuation)
                           (contract/wrap-contract contracts 'my/ef)
                           (context/wrap-context))
          handlers     (sut/handlers)
          perform      (-> e/perform
                           (sut/wrap-tx (:transactable system)))
          ctx          {}
          [ctx result] (perform handlers continuation [ctx args])]
        result)))

(t/deftest user-get-by-id
  (let [ef   (fn [id]
               (with-effects
                 (! (effect :persistence.user/get-by-id id))))]
    (t/testing "not-found"
      (t/is (= nil (run ef 42))))
    (t/testing "fixture"
      (let [user (agg/build {:db/ident             :root
                             :agg/id               -1
                             :user/state           "active"
                             :user/admin?          true
                             :user/author?         true
                             :user/login           "admin"
                             :user/password-digest "digest"}
                            {:author.translation/author     :root
                             :author.translation/lang       "en"
                             :author.translation/first-name "John"
                             :author.translation/last-name  "Doe"}
                            {:author.translation/author     :root
                             :author.translation/lang       "ru"
                             :author.translation/first-name "Иван"
                             :author.translation/last-name  "Иванов"})]
        (t/is (= user (run ef -1)))))))

(t/deftest user-create
  (let [user              (agg/build {:db/ident             :root
                                      :user/state           "active"
                                      :user/admin?          true
                                      :user/author?         true
                                      :user/login           "admin"
                                      :user/password-digest "digest"}
                                     {:author.translation/author     :root
                                      :author.translation/lang       "en"
                                      :author.translation/first-name "John"
                                      :author.translation/last-name  "Doe"}
                                     {:author.translation/author     :root
                                      :author.translation/lang       "ru"
                                      :author.translation/first-name "Иван"
                                      :author.translation/last-name  "Иванов"})
        ef                (fn [user]
                            (with-effects
                              (let [saved     (! (effect :persistence.user/create user))
                                    id        (agg/val-in saved :root :agg/id)
                                    refreshed (! (effect :persistence.user/get-by-id id))]
                                [saved refreshed])))
        [saved refreshed] (run ef user)]
    (t/is (= saved refreshed))
    (t/is (= user
             (d/db-with saved     [[:db.fn/retractAttribute :root :agg/id]])
             (d/db-with refreshed [[:db.fn/retractAttribute :root :agg/id]])))))
