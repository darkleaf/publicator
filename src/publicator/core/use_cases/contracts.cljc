(ns publicator.core.use-cases.contracts
  (:require
   [darkleaf.effect.middleware.contract :as contract]
   [darkleaf.effect.middleware.state :as state]
   [datascript.core :as d]
   [publicator.core.domain.aggregate :as agg]))

(defonce registry (atom {}))

(swap! registry merge
       state/contract
       {:persistence.user/create           {:effect   (fn [user]
                                                        (and (d/db? user)
                                                             (not (agg/include? user :root :agg/id))))
                                            :coeffect (fn [user]
                                                        (and (d/db? user)
                                                             (agg/include? user :root :agg/id)))}
        :persistence.user/exists-by-login  {:effect   (fn [login] (string? login))
                                            :coeffect boolean?}
        :persistence.user/get-by-login     {:effect   (fn [login] (string? login))
                                            :coeffect (some-fn nil? d/db?)}
        :persistence.user/get-by-id        {:effect   (fn [id] (int? id))
                                            :coeffect (some-fn nil? d/db?)}
        :persistence.user/update           {:effect   (fn [user] (d/db? user))
                                            :coeffect d/db?}
        :persistence.user/asc-by-login     {:effect   (fn [] true)
                                            :coeffect #(every? d/db? %)}
        :persistence.publication/get-by-id {:effect   (fn [id] (int? id))
                                            :coeffect (some-fn nil? d/db?)}
        :hasher/derive                     {:effect   (fn [password] (string? password))
                                            :coeffect string?}
        :hasher/check                      {:effect   (fn [password digest]
                                                        (and (string? password)
                                                             (string? digest)))
                                            :coeffect boolean?}})
