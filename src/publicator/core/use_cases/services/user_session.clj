(ns publicator.core.use-cases.services.user-session
  (:require
   [darkleaf.effect.core :refer [with-effects ! effect]]
   [datascript.core :as d]))

(defn user-id []
  (with-effects
    (let [session (! (effect [:session/get]))]
      (::id session))))

(defn logged-in? []
  (with-effects
    (boolean (! (user-id)))))

(defn logged-out? []
  (with-effects
    (not (! (logged-in?)))))

(defn log-in! [user]
  (let [id (d/q '[:find ?v . :where [:root :agg/id ?v]] user)]
    (effect [:session/swap assoc ::id id])))

(defn log-out! []
  (effect [:session/swap dissoc ::id]))

(defn user []
  (with-effects
    (when-let [id (! (user-id))]
      (! (effect [:persistence.user/get-by-id id])))))
