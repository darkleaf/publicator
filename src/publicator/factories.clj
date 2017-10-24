(ns publicator.factories
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sgen]
            [publicator.interactors.abstractions.storage :as storage]
            [publicator.domain.user :as user]))

(defn create-user [& {:as params}]
  (-> (s/gen ::user/build-params)
      (sgen/generate)
      (merge params)
      (user/build)
      (storage/tx-create)))
