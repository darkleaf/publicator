(ns publicator.fixtures
  (:require
   [clojure.test :as t]
   [publicator.fake.hasher :as hasher]
   [publicator.fake.id-generator :as id-generator]
   [publicator.fake.post-queries :as post-q]
   [publicator.fake.session :as session]
   [publicator.fake.storage :as storage]
   [publicator.fake.user-queries :as user-q]))

(defn implementations [f]
  (let [db (storage/build-db)
        binding-map (reduce merge [(storage/binding-map db)
                                   (session/binding-map)
                                   (user-q/binding-map db)
                                   (post-q/binding-map db)
                                   (hasher/binding-map)
                                   (id-generator/binding-map)])]
    (with-bindings binding-map
      (f))))

(def all (t/join-fixtures [implementations]))
