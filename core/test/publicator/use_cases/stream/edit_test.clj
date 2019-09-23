(ns publicator.use-cases.stream.edit-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.use-cases.stream.edit :as edit]
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

(t/deftest process-success
  (let [tx-data [{:stream.translation/stream :root
                  :stream.translation/lang   :ru
                  :stream.translation/name   "Новый Поток"}]
        script  [{:coeffect [1 tx-data]}
                 {:effect   [:session/get]
                  :coeffect {:current-user-id 1}}
                 {:effect   [:persistence/find :agg/user 1]
                  :coeffect user}
                 {:effect   [:persistence/find :agg/stream 1]
                  :coeffect stream}
                 {:effect [:do
                           [:persistence/save (agg/apply-tx stream tx-data)]
                           [:ui/show-main-screen]]}]]
    (u/test-with-script edit/process script)))
