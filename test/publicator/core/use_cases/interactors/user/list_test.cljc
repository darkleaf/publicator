(ns publicator.core.use-cases.interactors.user.list-test
  (:require
   [clojure.test :as t]
   [darkleaf.effect.core :refer [effect]]
   [darkleaf.generator.core :as gen]
   [datascript.core :as d]
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.use-cases.interactors.test-common :as tc]
   [publicator.core.use-cases.interactors.user.list :as user.list]))

(t/deftest process-success
  (let [users [(agg/build {:db/ident             :root
                           :agg/id               1
                           :user/login           "alice"
                           :user/password-digest "digest"
                           :user/state           "active"})
               (agg/build {:db/ident             :root
                           :agg/id               2
                           :user/login           "john"
                           :user/password-digest "digest"
                           :user/state           "active"
                           :user/author?         true}
                          {:author.translation/author     :root
                           :author.translation/lang       "en"
                           :author.translation/first-name "John"
                           :author.translation/last-name  "Doe"}
                          {:author.translation/author     :root
                           :author.translation/lang       "ru"
                           :author.translation/first-name "Иван"
                           :author.translation/last-name  "Иванов"})]
        views [{:agg/id              1
                :user/login          "alice"
                :user/state          "active"
                :control/can-update? false}
               {:agg/id                        2
                :user/login                    "john"
                :user/state                    "active"
                :author.translation/first-name "John"
                :author.translation/last-name  "Doe"
                :control/can-update?           false}]

        f*  (tc/wrap #'user.list/process*)
        ctx {}
        gen (f* ctx)]
    (t/is (= (effect :persistence.user/asc-by-login)
             (gen/value gen)))
    (gen/next gen users)
    (t/is (= (effect ::user.list/->processed views)
             (gen/value gen)))
    (gen/return gen)
    (t/is (gen/done? gen))
    (t/is (= [{} nil] (gen/value gen)))))
