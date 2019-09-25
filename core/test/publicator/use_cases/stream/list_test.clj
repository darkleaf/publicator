(ns publicator.use-cases.stream.list-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.use-cases.stream.list :as list]
   [clojure.test :as t]
   [publicator.util :as u]))

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
  (let [session {:current-user-id 1}
        view    {:agg/id           1
                 :stream.view/name "Поток"
                 :ui/can-edit?     true}
        script  [{:coeffect nil}
                 {:effect   [:persistence/active-streams]
                  :coeffect [stream]}

                 {:effect   [:next [[] [stream]]]
                  :coeffect [[] [stream]]}
                 {:effect   [:session/get]
                  :coeffect session}
                 {:effect   [:session/get]
                  :coeffect session}
                 {:effect   [:persistence/find :agg/user 1]
                  :coeffect user}
                 {:effect   [:next [[view] nil]]
                  :coeffect [[view] nil]}

                 {:effect [:ui/render-streams [view]]}]]
    (u/test-with-script list/process script)))
