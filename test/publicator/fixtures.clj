(ns publicator.fixtures
  (:require
   [publicator.fakes.storage :as storage]
   [publicator.fakes.session :as session]
   [publicator.fakes.user-queries :as user-q]
   [publicator.fakes.post-queries :as post-q]
   [publicator.fakes.hasher :as hasher]
   [publicator.fakes.id-generator :as id-generator]
   [clojure.test :as t]))

(defn implementations [f]
  (let [db (storage/build-db)
        binding-map (reduce merge [(storage/binding-map db)
                                   (session/binding-map)
                                   (user-q/binging-map db)
                                   (post-q/binging-map db)
                                   (hasher/binding-map)
                                   (id-generator/binging-map)])]
    (with-bindings binding-map
      (f))))

(def all (t/join-fixtures [implementations]))
