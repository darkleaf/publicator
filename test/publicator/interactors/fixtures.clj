(ns publicator.interactors.fixtures
  (:require
   [publicator.domain.utils.password :as password]

   [publicator.interactors.abstractions.storage :as storage]
   [publicator.fakes.storage :as fakes.storage]

   [publicator.interactors.abstractions.session :as session]
   [publicator.fakes.session :as fakes.session]

   [publicator.interactors.abstractions.user-queries :as user-q]
   [publicator.fakes.user-queries :as fakes.user-q]

   [clojure.test :as t]))

(defn fake-password [f]
  (with-redefs-fn {#'password/encrypt identity
                   #'password/check =}
    f))

(defn implementations [f]
  (let [db (fakes.storage/build-db)]
    (binding [session/*session*     (fakes.session/build)
              storage/*storage*     (fakes.storage/build-storage db)
              user-q/*get-by-login* (fakes.user-q/build-get-by-login db)]
      (f))))

(def all (t/join-fixtures [fake-password implementations]))
