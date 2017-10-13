(ns publicator.system
  (:require
   [publicator.components.pedestal :as pedestal]
   [publicator.components.db :as db]
   [publicator.components.interactor-ctx :as interactor-ctx]
   [com.stuartsierra.component :as component]))

(defn build []
  (component/system-map
   :db (db/build)
   :interactor-ctx (component/using (interactor-ctx/build) [:db])
   :pedestal (component/using (pedestal/build) [:interactor-ctx])))
