(ns publicator.factories
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sgen]
            [publicator.interactors.abstractions.storage :as storage]
            [publicator.domain.user :as user]
            [publicator.domain.post :as post]))

(defn create-user [& {:as params}]
  (-> (s/gen ::user/build-params)
      (sgen/generate)
      (merge params)
      (user/build)
      (storage/tx-create)))

(defn create-post [& {:keys [author-id] :as params}]
  (-> (s/gen ::post/build-params)
      (sgen/generate)
      (merge params)
      (merge (when-not author-id
               {:author-id (:id (create-user))}))
      (post/build)
      (storage/tx-create)))
