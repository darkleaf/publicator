(ns publicator.core.use-cases.stream.edit-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.use-cases.stream.edit :as edit]
   [clojure.test :as t]
   [darkleaf.effect.core :as e]
   [darkleaf.effect.script :as script]
   [datascript.core :as d]))

(t/deftest process-success
  (let [session      {:current-user-id 1}
        user         (-> (agg/allocate)
                         (d/db-with [{:db/ident             :root
                                      :agg/id               1
                                      :user/login           "admin"
                                      :user/password-digest "digest"
                                      :user/state           :active
                                      :user/role            :admin}]))
        stream       (-> (agg/allocate)
                         (d/db-with [{:db/ident     :root
                                      :agg/id       1
                                      :stream/state :active}
                                     {:stream.translation/stream :root
                                      :stream.translation/lang   :en
                                      :stream.translation/name   "Stream"}
                                     {:stream.translation/stream :root
                                      :stream.translation/lang   :ru
                                      :stream.translation/name   "Поток"}]))
        script       [{:args [1]}
                      {:tag      10
                       :effect   [:session/get]
                       :coeffect session}
                      {:key      20
                       :effect   [:persistence.user/get-by-id 1]
                       :coeffect user}
                      {:tag      30
                       :effect   [:persistence.stream/get-by-id 1]
                       :coeffect stream}
                      {:tag      40
                       :effect   [:session/get]
                       :coeffect session}
                      {:tag      50
                       :effect   [:persistence.user/get-by-id 1]
                       :coeffect user}
                      {:tag      60
                       :effect   [:ui.form/edit stream]
                       :coeffect [{:db/id                   3
                                   :stream.translation/name "Новый Поток"}]}
                      {:tag      70
                       :effect   [:persistence.stream/update
                                  (-> (agg/allocate)
                                      (d/db-with [{:db/ident     :root
                                                   :agg/id       1
                                                   :stream/state :active}
                                                  {:stream.translation/stream :root
                                                   :stream.translation/lang   :en
                                                   :stream.translation/name   "Stream"}
                                                  {:stream.translation/stream :root
                                                   :stream.translation/lang   :ru
                                                   :stream.translation/name   "Новый Поток"}]))]
                       :coeffect nil}
                      {:tag          80
                       :final-effect [:ui/show-main-screen]}]
        continuation (e/continuation edit/process)]
    (script/test continuation script)))
