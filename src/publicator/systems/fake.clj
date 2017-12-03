(ns publicator.systems.fake
  (:require
   [com.stuartsierra.component :as component]
   [publicator.components.fakes :as fakes]
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

(defn build [{:keys [http-opts]}]
  (component/system-map
   :implementations (fakes/build)
   :seed (component/using
          (Seed. nil)
          [:implementations])
   :jetty (component/using
           (jetty/build http-opts)
           [:implementations])))
