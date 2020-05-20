(ns publicator.core.use-cases.contracts
  (:require
   [darkleaf.effect.middleware.contract :as contract]
   [datascript.core :as d]))

(defonce registry (atom {}))

(swap! registry assoc
       :session/get
       {:effect   (fn [[_]] true)
        :coeffect map?}

       :session/swap
       {:effect   (fn [[_ f]] (ifn? f))
        :coeffect map?}

       :persistence.user/create
       {:effect   (fn [[_ user]] (d/db? user))
        :coeffect d/db?}

       :persistence.user/exists-by-login
       {:effect   (fn [[_ login]] (string? login))
        :coeffect boolean?}

       :hasher/derive
       {:effect   (fn [[_ value]] (string? value))
        :coeffect string?})
