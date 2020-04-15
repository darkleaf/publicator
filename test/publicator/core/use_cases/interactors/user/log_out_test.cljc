(ns publicator.core.use-cases.interactors.user.log-out-test
  (:require
   [publicator.core.use-cases.interactors.user.log-out :as log-out]
   [publicator.core.use-cases.services.user-session :as user-session]
   #_[publicator.core.domain.aggregate :as agg]
   [darkleaf.effect.core :as e]
   [darkleaf.effect.script :as script]
   [clojure.test :as t]
   #_[datascript.core :as d]))

(t/deftest process-success
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {::user-session/id 1}}
                      {:effect [:session/swap dissoc ::user-session/id]}
                      {:final-effect [::log-out/->processed]}]
        continuation (e/continuation log-out/process)]
    (script/test continuation script)))
