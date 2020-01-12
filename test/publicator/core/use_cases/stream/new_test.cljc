(ns publicator.core.use-cases.stream.new-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.use-cases.stream.new :as new]
   [clojure.test :as t]
   [darkleaf.effect.core :as e]
   [darkleaf.effect.script :as script]))

(def user
  (-> (agg/allocate :agg/user)
      (agg/apply-tx [{:db/ident             :root
                      :agg/id               1
                      :user/login           "admin"
                      :user/password-digest "digest"
                      :user/state           :active
                      :user/role            :admin}])))

(t/deftest process-success
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {:current-user-id 1}}
                      {:effect   [:persistence/find :agg/user 1]
                       :coeffect user}
                      {:effect   [:ui.form/edit (agg/allocate :agg.stream/base)]
                       :coeffect [{:stream.translation/stream :root
                                   :stream.translation/lang   :en
                                   :stream.translation/name   "Stream"}
                                  {:stream.translation/stream :root
                                   :stream.translation/lang   :ru
                                   :stream.translation/name   "Поток"}]}
                      {:effect   [:persistence/next-id :stream]
                       :coeffect 42}
                      {:effect   [:persistence/create
                                  (-> (agg/allocate :agg/stream)
                                      (agg/apply-tx [{:db/ident     :root
                                                      :agg/id       42
                                                      :stream/state :active}
                                                     {:stream.translation/stream :root
                                                      :stream.translation/lang   :en
                                                      :stream.translation/name   "Stream"}
                                                     {:stream.translation/stream :root
                                                      :stream.translation/lang   :ru
                                                      :stream.translation/name   "Поток"}]))]
                       :coeffect nil}
                      {:final-effect [:ui.screen/show :main]}]
        continuation (e/continuation new/process)]
    (script/test continuation script)))
