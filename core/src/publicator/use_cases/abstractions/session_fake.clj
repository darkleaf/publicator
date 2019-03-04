(ns publicator.use-cases.abstractions.session-fake
  (:require
   [publicator.use-cases.abstractions.session :as session]))

(defn binding-map []
  (let [storage (atom {})]
    {#'session/*get* (fn [k] (get @storage k))
     #'session/*set* (fn [k v] (swap! storage assoc k v))}))
