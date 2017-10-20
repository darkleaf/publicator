(ns publicator.system
  (:require
   [publicator.components
    [pedestal :as pedestal]
    [db :as db]
    [implementations :as implementations]]
   [com.stuartsierra.component :as component]))

(defn build []
  (component/system-map
   :db (db/build)
   :implementations (component/using (implementations/build) [:db])
   :pedestal (component/using (pedestal/build) [:implementations])))
