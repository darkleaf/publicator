(ns publicator.core.use-cases.stream.edit-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.use-cases.stream.edit :as edit]
   [clojure.test :as t]
   [darkleaf.effect.core :as e]
   [darkleaf.effect.script :as script]))

(t/deftest process-success
  (let [script       [{:args [1]}
                      {:effect   [:session/get]
                       :coeffect {:current-user-id 1}}
                      {:effect   [:persistence/find :agg/user 1]
                       :coeffect (-> (agg/allocate :agg/user)
                                     (agg/apply-tx [{:db/ident             :root
                                                     :agg/id               1
                                                     :user/login           "admin"
                                                     :user/password-digest "digest"
                                                     :user/state           :active
                                                     :user/role            :admin}]))}
                      {:effect   [:persistence/find :agg/stream 1]
                       :coeffect (-> (agg/allocate :agg/stream)
                                     (agg/apply-tx! [{:db/ident     :root
                                                      :agg/id       1
                                                      :stream/state :active}
                                                     {:stream.translation/stream :root
                                                      :stream.translation/lang   :en
                                                      :stream.translation/name   "Stream"}
                                                     {:stream.translation/stream :root
                                                      :stream.translation/lang   :ru
                                                      :stream.translation/name   "Поток"}]))}
                      {:effect   [:ui.form/edit
                                  (-> (agg/allocate :agg.stream/base)
                                      (agg/apply-tx! [{:stream.translation/stream :root
                                                       :stream.translation/lang   :en
                                                       :stream.translation/name   "Stream"}
                                                      {:stream.translation/stream :root
                                                       :stream.translation/lang   :ru
                                                       :stream.translation/name   "Поток"}]))]
                       :coeffect [{:stream.translation/stream :root
                                   :stream.translation/lang   :ru
                                   :stream.translation/name   "Новый Поток"}]}
                      {:effect   [:persistence/update
                                  (-> (agg/allocate :agg/stream)
                                      (agg/apply-tx! [{:db/ident     :root
                                                       :agg/id       1
                                                       :stream/state :active}
                                                      {:stream.translation/stream :root
                                                       :stream.translation/lang   :en
                                                       :stream.translation/name   "Stream"}
                                                      {:stream.translation/stream :root
                                                       :stream.translation/lang   :ru
                                                       :stream.translation/name   "Новый Поток"}]))]
                       :coeffect nil}
                      {:final-effect [:ui/show-main-screen]}]
        continuation (e/continuation edit/process)]
    (script/test continuation script)))
