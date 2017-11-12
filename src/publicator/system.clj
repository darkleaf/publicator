(ns publicator.system
  (:require
   [publicator.components
    [jetty :as jetty]
    [implementations :as implementations]]
   [com.stuartsierra.component :as component]))

(defn build []
  (component/system-map
   :implementations (implementations/build)
   :jetty (component/using (jetty/build) [:implementations])))
