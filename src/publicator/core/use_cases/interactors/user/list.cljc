(ns publicator.core.use-cases.interactors.user.list
  (:require
   [darkleaf.effect.core :refer [with-effects effect !]]
   [darkleaf.effect.core-analogs :as ecore]
   [datascript.core :as d]
   [publicator.core.use-cases.contracts :as contracts]
   [publicator.core.use-cases.interactors.user.update :as update]
   [publicator.core.use-cases.services.user-session :as user-session]
   [publicator.utils :refer [<<-]]))

(defn- user->view [user]
  (with-effects
    (let [lang    (! (user-session/language))
          control {:control/can-update? (= :pass (! (update/precondition user)))}
          root    (d/pull user [:agg/id :user/login :user/state] :root)
          trans   (d/q '[:find (pull ?e [:author.translation/first-name
                                         :author.translation/last-name]) .
                         :in $ ?lang
                         :where
                         [?e :author.translation/author :root]
                         [?e :author.translation/lang ?lang]]
                       user lang)]
      (merge root trans control))))

(defn precondition []
  :pass)

(defn process []
  (<<-
   (with-effects)
   (do (! (! (precondition))))
   (let [users (! (effect :persistence.user/asc-by-login))
         views (! (ecore/mapv! user->view users))])
   (! (effect ::->processed views))))

(swap! contracts/registry merge
       {`process      {:args (fn [] true)}
        ::->processed {:effect (fn [views] (every? map? views))}})
