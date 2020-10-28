(ns publicator.core.use-cases.interactors.test-common
  (:require
   [darkleaf.effect.core :as e]
   [darkleaf.effect.middleware.contract :as contract]
   [darkleaf.effect.middleware.state :as state]
   [publicator.core.use-cases.contracts :as contracts]))

(defn wrap [var*]
  (let [f*     (deref var*)
        f-name (symbol var*)]
    (-> f*
        (e/wrap)
        (contract/wrap-contract @contracts/registry f-name)
        (state/wrap-state))))
