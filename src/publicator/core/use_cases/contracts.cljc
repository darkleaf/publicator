(ns publicator.core.use-cases.contracts
  (:require
   [darkleaf.effect.middleware.contract :as contract]
   [datascript.core :as d]))

(defonce registry (atom {}))

(swap! registry assoc
       :session/get
       {:effect   (fn [] true)
        :coeffect map?}

       :session/swap
       {:effect   (fn [f & args] (ifn? f))
        :coeffect map?}

       :persistence.user/create
       {:effect   (fn [user] (d/db? user))
        :coeffect d/db?}

       :persistence.user/exists-by-login
       {:effect   (fn [login] (string? login))
        :coeffect boolean?}

       :hasher/derive
       {:effect   (fn [value] (string? value))
        :coeffect string?})
