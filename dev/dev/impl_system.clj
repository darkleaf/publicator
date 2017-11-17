(ns dev.impl-system
  (:require
   [com.stuartsierra.component :as component]
   [dev.impls :as impls]
   [publicator.components.data-source :as data-source]
   [publicator.components.jetty :as jetty]))


(defn build []
  (component/system-map
   :data-source (data-source/build {:jdbc-url "jdbc:postgresql://db/development"
                                    :user "postgres"
                                    :password "password"})
   :implementations (component/using
                     (impls/build)
                     [:data-source])
   :jetty (component/using
           (jetty/build {:host "0.0.0.0", :port 4102})
           [:implementations])))
