(ns dev.fake-system
  (:require
   [com.stuartsierra.component :as component]
   [dev.fakes :as fakes]
   [dev.seed :as seed]
   [publicator.components.jetty :as jetty]))

(defrecord Seed [implementations]
  component/Lifecycle
  (start [this]
    (seed/seed (:binding-map implementations))
    this)
  (stop [this]
    this))

(defn build []
  (component/system-map
   :implementations (fakes/build)
   :seed (component/using
          (Seed. nil)
          [:implementations])
   :jetty (component/using
           (jetty/build {:host "0.0.0.0", :port 4101})
           [:implementations])))
