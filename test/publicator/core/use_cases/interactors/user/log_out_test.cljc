(ns publicator.core.use-cases.interactors.user.log-out-test
  (:require
   [clojure.test :as t]
   [darkleaf.effect.core :as e]
   [darkleaf.effect.middleware.contract :as contract]
   [darkleaf.effect.script :as script]
   [publicator.core.use-cases.contracts :as contracts]
   [publicator.core.use-cases.interactors.user.log-out :as log-out]
   [publicator.core.use-cases.services.user-session :as user-session]))

(t/deftest process-success
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {::user-session/id 1}}
                      {:effect   [:session/swap dissoc ::user-session/id]
                       :coeffect {}}
                      {:final-effect [::log-out/->processed]}]
        continuation (-> log-out/process
                         (e/continuation)
                         (contract/wrap-contract @contracts/registry `log-out/process))]
    (script/test continuation script)))
