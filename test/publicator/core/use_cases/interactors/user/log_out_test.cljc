(ns publicator.core.use-cases.interactors.user.log-out-test
  (:require
   [clojure.test :as t]
   [darkleaf.effect.core :refer [effect]]
   [darkleaf.generator.core :as gen]
   [publicator.core.use-cases.interactors.user.log-out :as user.log-out]
   [publicator.core.use-cases.services.user-session :as user-session]
   [publicator.core.use-cases.interactors.test-common :as tc]))

(t/deftest process-success
  (let [f*  (tc/wrap #'user.log-out/process*)
        ctx {:session {::user-session/id 1}}
        gen (f* ctx)]
    (t/is (= (effect ::user.log-out/->processed)
             (gen/value gen)))
    (gen/return gen)
    (t/is (gen/done? gen))
    (t/is (= [{} nil] (gen/value gen)))))

(t/deftest process-already-logged-out
  (let [f*  (tc/wrap #'user.log-out/process*)
        ctx {}
        gen (f* ctx)]
    (t/is (= (effect ::user.log-out/->already-logged-out)
             (gen/value gen)))
    (gen/return gen)
    (t/is (gen/done? gen))
    (t/is (= [{} nil] (gen/value gen)))))
