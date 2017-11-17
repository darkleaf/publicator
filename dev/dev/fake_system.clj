(ns dev.fake-system
  (:require
   [com.stuartsierra.component :as component]
   [dev.fakes :as fakes]
   [publicator.factories :as factories]
   [publicator.components.jetty :as jetty]))

(defn- seed [bindig-map]
  (with-bindings bindig-map
    (let [admin (factories/create-user :login "admin"
                                       :password "12345678"
                                       :full-name "Admin")]
      (factories/create-post :author-id (:id admin))
      (factories/create-post))))

(defrecord Seed [implementations]
  component/Lifecycle
  (start [this]
    (seed (:binding-map implementations))
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
