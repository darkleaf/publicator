(ns publicator.controllers.test-ring
  (:require
   [publicator.ring.handler :as handler]
   [publicator.fakes.storage :as storage]
   [publicator.fakes.user-queries :as user-q]
   [publicator.fakes.post-queries :as post-q]
   [publicator.fakes.hasher :as hasher]
   [publicator.fakes.id-generator :as id-generator]))

(defn build-handler []
  (let [db (storage/build-db)
        binding-map (reduce merge [(storage/binding-map db)
                                   (user-q/binging-map db)
                                   (post-q/binging-map db)
                                   (hasher/binding-map)
                                   (id-generator/binging-map)])]
    (handler/build binding-map)))
