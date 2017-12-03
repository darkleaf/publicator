(ns publicator.systems.impl
  (:require
   [com.stuartsierra.component :as component]
   [publicator.components.impls :as impls]
   [publicator.components.data-source :as data-source]
   [publicator.components.jetty :as jetty]
   [publicator.db.migration :as migration]))

(defrecord Migration [data-source]
  component/Lifecycle
  (start [this]
    (migration/migrate (:data-source data-source))
    this)
  (stop [this]
    this))

(defn build [{:keys [http-opts data-source-opts]}]
  (component/system-map
   :data-source (data-source/build data-source-opts)
   :implementations (component/using
                     (impls/build)
                     [:data-source])
   :jetty (component/using
           (jetty/build http-opts)
           [:implementations])
   :migration (component/using
               (Migration. nil)
               [:data-source])))
