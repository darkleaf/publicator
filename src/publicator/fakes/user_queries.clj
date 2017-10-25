(ns publicator.fakes.user-queries
  (:require
   [publicator.interactors.abstractions.user-queries :as user-q])
  (:import
   [publicator.domain.user User]))

(deftype GetByLogin [db]
  user-q/GetByLogin
  (-get-by-login [_ login]
    (->> db
         (deref)
         (vals)
         (map deref)
         (filter #(instance? User %))
         (filter #(= login (:login %)))
         (first))))

(defn binging-map [db]
  {#'user-q/*get-by-login* (->GetByLogin db)})
