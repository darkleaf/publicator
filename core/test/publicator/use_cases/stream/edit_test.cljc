(ns publicator.use-cases.stream.edit-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.use-cases.stream.edit :as edit]
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

(t/deftest process-success
  (let [tx-data [{:db/ident     :root
                  :stream/state :archived}
                 {:stream.translation/stream :root
                  :stream.translation/lang   :ru
                  :stream.translation/name   "Новый Поток"}]
        script  [{:args [1]}
                 {:effect   [:session/get]
                  :coeffect {:current-user-id 1}}
                 {:effect   [:persistence/find :agg/user 1]
                  :coeffect user}
                 {:effect   [:persistence/find :agg/stream 1]
                  :coeffect stream}
                 {:effect   [:ui/edit stream]
                  :coeffect tx-data}
                 {:effect   [:persistence/save (agg/apply-tx stream tx-data)]
                  :coeffect nil}
                 {:final-effect [:ui/show-main-screen]}]]
    (e/test edit/process script)))
