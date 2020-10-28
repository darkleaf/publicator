(ns publicator.core.use-cases.services.user-session
  (:require
   [publicator.core.domain.languages :as langs]
   [darkleaf.generator.core :refer [generator yield]]
   [darkleaf.effect.core :refer [effect]]
   [darkleaf.effect.middleware.state :as state]
   [datascript.core :as d]
   [medley.core :as m]))

(defn user-id* []
  (generator
    (yield (effect ::state/gets get-in [:session ::id]))))

(defn logged-in?* []
  (generator
    (boolean (yield (user-id*)))))

(defn logged-out?* []
  (generator
    (not (yield (logged-in?*)))))

(defn log-in* [user]
  (let [id (d/q '[:find ?v . :where [:root :agg/id ?v]] user)]
    (effect ::state/modify assoc-in [:session ::id] id)))

(defn log-out* []
  (effect ::state/modify m/dissoc-in [:session ::id]))

(defn user* []
  (generator
    (when-let [id (yield (user-id*))]
      (yield (effect :persistence.user/get-by-id id)))))

(defn language* []
  (effect ::state/gets get-in [:session ::lang] (first langs/languages)))
