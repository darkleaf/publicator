(ns publicator.interactors.fixtures
  (:require
   [publicator.fakes.storage :as fakes.storage]
   [publicator.fakes.session :as fakes.session]
   [publicator.fakes.user-queries :as fakes.user-q]
   [publicator.fakes.hasher :as fakes.hasher]
   [publicator.fakes.id-generator :as fakes.id-generator]

   [clojure.test :as t]))

(defn implementations [f]
  (let [db (fakes.storage/build-db)
        binding-map (reduce merge [(fakes.storage/binding-map db)
                                   (fakes.session/binding-map)
                                   (fakes.user-q/binging-map db)
                                   (fakes.hasher/binding-map)
                                   (fakes.id-generator/binging-map)])]
    (with-bindings binding-map
      (f))))

(def all (t/join-fixtures [implementations]))
