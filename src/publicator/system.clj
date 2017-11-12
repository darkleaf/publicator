(ns publicator.system
  (:require
   [publicator.components
    [jetty :as jetty]
    [fake-impl :as fake-impl]]
   [com.stuartsierra.component :as component]))

(defn build []
  (component/system-map
   :implementations (fake-impl/build)
   :jetty (component/using (jetty/build) [:implementations])))
