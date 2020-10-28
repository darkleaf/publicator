(ns publicator.core.use-cases.interactors.user.log-out
  (:require
   [darkleaf.effect.core :refer [effect]]
   [darkleaf.generator.core :refer [generator yield]]
   [publicator.core.use-cases.contracts :as contracts]
   [publicator.core.use-cases.services.user-session :as user-session]))

(defn precondition** []
  (generator
    (if (yield (user-session/logged-out?*))
      (effect ::->already-logged-out)
      :pass)))

(defn process* []
  (generator
    (yield (yield (precondition**)))
    (yield (user-session/log-out*))
    (yield (effect ::->processed))))

(swap! contracts/registry merge
       {`process*              {:args   (fn [] true)
                                :return nil?}
        ::->processed          {:effect (fn [] true)}
        ::->already-logged-out {:effect (fn [] true)}})
