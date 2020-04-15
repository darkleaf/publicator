(ns publicator.core.use-cases.interactors.user.log-out
  (:require
   [publicator.core.use-cases.services.user-session :as user-session]
   [darkleaf.effect.core :refer [with-effects ! effect]]))

(defn precondition []
  (with-effects
    (if (! (user-session/logged-out?))
      (effect [::->already-logged-out]))))

(defn process []
  (with-effects
    (! (! (precondition)))
    (! (user-session/log-out!))
    (! (effect [::->processed]))))
