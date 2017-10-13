(ns publicator.pedestal.session
  (:require
   [publicator.interactors.abstractions.session :as abstractions.session]))

(deftype Session [storage]
  abstractions.session/Session
  (-get [_ k] (get @storage k))
  (-set! [_ k v] (swap! storage assoc k v)))

(defn build []
  {:name  ::session
   :enter (fn [context]
            (let [storage (atom (get-in context [:request :session]))
                  session (Session. storage)]
              (-> context
                  (assoc ::storage storage)
                  (assoc-in [:request :interactor-ctx ::abstractions.session/session]
                            session))))
   :leave (fn [context]
            (let [storage (::storage context)]
              (-> context
                  (assoc-in [:response :session/key]
                            (get-in context [:request :session/key]))
                  (assoc-in [:response :session] @storage))))})
