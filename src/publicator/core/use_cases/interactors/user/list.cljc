(ns publicator.core.use-cases.interactors.user.list
  (:require
   [darkleaf.effect.core :refer [effect]]
   [darkleaf.generator.core :as gen :refer [generator yield]]
   [datascript.core :as d]
   [publicator.core.use-cases.contracts :as contracts]
   [publicator.core.use-cases.interactors.user.update :as update]
   [publicator.core.use-cases.services.user-session :as user-session]
   [publicator.utils :refer [<<-]]))

(defn- user->view* [user]
  (generator
    (let [lang    (yield (user-session/language*))
          control {:control/can-update? (= :pass (yield (update/precondition** user)))}
          root    (d/pull user [:agg/id :user/login :user/state] :root)
          trans   (d/q '[:find (pull ?e [:author.translation/first-name
                                         :author.translation/last-name]) .
                         :in $ ?lang
                         :where
                         [?e :author.translation/author :root]
                         [?e :author.translation/lang ?lang]]
                       user lang)]
      (merge root trans control))))

(defn precondition** []
  :pass)

(defn process* []
  (<<-
   (generator)
   (do (yield (yield (precondition**))))
   (let [users (yield (effect :persistence.user/asc-by-login))
         views (yield (gen/mapv* user->view* users))])
   (yield (effect ::->processed views))))

(swap! contracts/registry merge
       {`process*     {:args   (fn [] true)
                       :return nil?}
        ::->processed {:effect (fn [views] (every? map? views))}})
