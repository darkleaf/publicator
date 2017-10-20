(ns publicator.interactors.fixtures
  (:require
   [publicator.interactors.abstractions.storage :as storage]
   [publicator.fakes.storage :as fakes.storage]

   [publicator.interactors.abstractions.session :as session]
   [publicator.fakes.session :as fakes.session]

   [publicator.interactors.abstractions.user-queries :as user-q]
   [publicator.fakes.user-queries :as fakes.user-q]

   [publicator.domain.abstractions.hasher :as hasher]
   [publicator.fakes.hasher :as fakes.hasher]

   [publicator.domain.abstractions.id-generator :as id-generator]
   [publicator.fakes.id-generator :as fakes.id-generator]

   [clojure.test :as t]))

(defn implementations [f]
  (let [db (fakes.storage/build-db)]
    (binding [session/*session*           (fakes.session/build)
              storage/*storage*           (fakes.storage/build-storage db)
              user-q/*get-by-login*       (fakes.user-q/build-get-by-login db)
              hasher/*hasher*             (fakes.hasher/build)
              id-generator/*id-generator* (fakes.id-generator/build)]
      (f))))

(def all (t/join-fixtures [implementations]))
