(ns publicator.persistence.handlers-test
  (:require
   [clojure.test :as t]
   [darkleaf.effect.core :refer [effect]]
   [darkleaf.generator.core :refer [generator yield]]
   [datascript.core :as d]
   [publicator.core.domain.aggregate :as agg]
   [publicator.persistence.test-system :as test-system]
   [publicator.core.use-cases.aggregates.user :as user]))

(t/deftest user-get-by-id
  (let [f* (fn [id]
             (generator
               (yield (effect :persistence.user/get-by-id id))))]
    (t/testing "not-found"
      (t/is (= nil (test-system/run f* 42))))
    (t/testing "fixture"
      (let [user (-> (agg/build {:db/ident             :root
                                 :agg/id               -1
                                 :user/state           "active"
                                 :user/admin?          true
                                 :user/author?         true
                                 :user/login           "admin"
                                 :user/password-digest "digest"}
                                {:translation/root              :root
                                 :translation/lang              :en
                                 :author.translation/first-name "John"
                                 :author.translation/last-name  "Doe"}
                                {:translation/root              :root
                                 :translation/lang              :ru
                                 :author.translation/first-name "Иван"
                                 :author.translation/last-name  "Иванов"}
                                {:author.achivement/root        :root
                                 :author.achivement/kind        "star"
                                 :author.achivement/assigner-id -1})
                     #_(user/validate)
                     #_(agg/check-errors))]
        (t/is (= user (test-system/run f* -1)))))))


;; (t/deftest user-create
;;   (let [user              (agg/build {:db/ident             :root
;;                                       :user/state           "active"
;;                                       :user/admin?          true
;;                                       :user/author?         true
;;                                       :user/login           "admin"
;;                                       :user/password-digest "digest"}
;;                                      {:author.translation/author     :root
;;                                       :author.translation/lang       "en"
;;                                       :author.translation/first-name "John"
;;                                       :author.translation/last-name  "Doe"}
;;                                      {:author.translation/author     :root
;;                                       :author.translation/lang       "ru"
;;                                       :author.translation/first-name "Иван"
;;                                       :author.translation/last-name  "Иванов"})
;;         ef                (fn [user]
;;                             (with-effects
;;                               (let [saved     (! (effect :persistence.user/create user))
;;                                     id        (agg/val-in saved :root :agg/id)
;;                                     refreshed (! (effect :persistence.user/get-by-id id))]
;;                                 [saved refreshed])))
;;         [saved refreshed] (run ef user)]
;;     (t/is (= saved refreshed))
;;     (t/is (= user
;;              (d/db-with saved     [[:db.fn/retractAttribute :root :agg/id]])
;;              (d/db-with refreshed [[:db.fn/retractAttribute :root :agg/id]])))))
