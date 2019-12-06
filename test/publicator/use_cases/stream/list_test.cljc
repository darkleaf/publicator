(ns publicator.use-cases.stream.list-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.use-cases.stream.list :as list]
   [clojure.test :as t]
   [darkleaf.effect.core :as e]))

(def user
  (-> (agg/allocate :agg/user)
      (agg/apply-tx [{:db/ident             :root
                      :agg/id               1
                      :user/login           "admin"
                      :user/password-digest "digest"
                      :user/state           :active
                      :user/role            :admin}])))

(def stream
  (-> (agg/allocate :agg/stream)
      (agg/apply-tx [{:db/ident     :root
                      :agg/id       1
                      :stream/state :active}
                     {:stream.translation/stream :root
                      :stream.translation/lang   :en
                      :stream.translation/name   "Stream"}
                     {:stream.translation/stream :root
                      :stream.translation/lang   :ru
                      :stream.translation/name   "Поток"}])))

(t/deftest process-success-with-admin
  (let [session      {:current-user-id 1}
        view         {:agg/id           1
                      :stream.view/name "Поток"
                      :ui/can-edit?     true}
        script       [{:args []}
                      {:effect   [:persistence/active-streams]
                       :coeffect [stream]}
                      {:effect   [:session/get]
                       :coeffect session}
                      {:effect   [:session/get]
                       :coeffect session}
                      {:effect   [:persistence/find :agg/user 1]
                       :coeffect user}
                      {:final-effect [:ui/render-streams [view]]}]
        continuation (e/continuation list/process)]
    (e/test continuation script)))
