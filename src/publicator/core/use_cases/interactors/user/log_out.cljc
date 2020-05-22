(ns publicator.core.use-cases.interactors.user.log-out
  (:require
   [darkleaf.effect.core :refer [with-effects ! effect]]
   [publicator.core.use-cases.contracts :as contracts]
   [publicator.core.use-cases.services.user-session :as user-session]))

(defn precondition []
  (with-effects
    (if (! (user-session/logged-out?))
      (effect ::->already-logged-out)
      :pass)))

(defn process []
  (with-effects
    (! (! (precondition)))
    (! (user-session/log-out!))
    (! (effect ::->processed))))

(swap! contracts/registry merge
       {`process      {:args (fn [] true)}
        ::->processed {:effect (fn [] true)}})
