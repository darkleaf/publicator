(ns publicator.system
  (:require
   [publicator.components.pedestal :as pedestal]
   [publicator.components.db :as db]
   [publicator.components.impl :as impl]
   [com.stuartsierra.component :as component]))


(defn build []
  (component/system-map
   :pedestal (pedestal/build)
   :db (db/build)
   :impl (component/using (impl/build) [:db])))
