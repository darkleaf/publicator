(ns publicator.core.use-cases.interactors.user.log-out-test
  (:require
   [clojure.test :as t]
   [darkleaf.effect.core :as e]
   [darkleaf.effect.middleware.contract :as contract]
   [darkleaf.effect.script :as script]
   [publicator.core.use-cases.contracts :as contracts]
   [publicator.core.use-cases.interactors.user.log-out :as user.log-out]
   [publicator.core.use-cases.services.user-session :as user-session]))

(t/deftest process-success
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {::user-session/id 1}}
                      {:effect   [:session/swap dissoc ::user-session/id]
                       :coeffect {}}
                      {:final-effect [::user.log-out/->processed]}]
        continuation (-> user.log-out/process
                         (e/continuation)
                         (contract/wrap-contract @contracts/registry `user.log-out/process))]
    (script/test continuation script)))

(t/deftest process-already-logged-out
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {}}
                      {:final-effect [::user.log-out/->already-logged-out]}]
        continuation (-> user.log-out/process
                         (e/continuation)
                         (contract/wrap-contract @contracts/registry `user.log-out/process))]
    (script/test continuation script)))
