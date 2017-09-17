(ns publicator.system
  (:require
   [publicator.components.pedestal :as pedestal]
   [com.stuartsierra.component :as component]))


(defn build []
  (component/system-map
   :pedestal (pedestal/build)))
