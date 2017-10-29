(ns publicator.system
  (:require
   [publicator.components
    [jetty :as jetty]
    [db :as db]
    [implementations :as implementations]]
   [com.stuartsierra.component :as component]))

(defn build []
  (component/system-map
   :db (db/build)
   :implementations (component/using (implementations/build) [:db])
   :jetty (component/using (jetty/build) [:implementations])))
