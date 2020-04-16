(ns publicator.core.use-cases.interactors.stream.new-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.use-cases.interactors.stream.new :as new]
   [publicator.core.use-cases.services.user-session :as user-session]
   [clojure.test :as t]
   [darkleaf.effect.core :as e]
   [darkleaf.effect.script :as script]
   [datascript.core :as d]))

(t/deftest form-success
  (let [session      {::user-session/id 1}
        user         (agg/allocate {:db/ident             :root
                                    :agg/id               1
                                    :user/login           "admin"
                                    :user/password-digest "digest"
                                    :user/state           :active
                                    :user/role            :admin})
        script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect session}
                      {:effect   [:persistence.user/get-by-id 1]
                       :coeffect user}
                      {:final-effect [::new/->form (agg/allocate)]}]
        continuation (e/continuation new/form)]
    (script/test continuation script)))

(t/deftest process-success
  (let [session      {::user-session/id 1}
        user         (agg/allocate {:db/ident             :root
                                    :agg/id               1
                                    :user/login           "admin"
                                    :user/password-digest "digest"
                                    :user/state           :active
                                    :user/role            :admin})
        form         (agg/allocate {:stream.translation/stream :root
                                    :stream.translation/lang   :en
                                    :stream.translation/name   "Stream"}
                                   {:stream.translation/stream :root
                                    :stream.translation/lang   :ru
                                    :stream.translation/name   "Поток"})
        stream       (d/db-with form [[:db/add :root :stream/state :active]])
        persisted    (d/db-with stream [[:db/add :root :agg/id 1]])
        script       [{:args [form]}
                      {:effect   [:session/get]
                       :coeffect session}
                      {:effect   [:persistence.user/get-by-id 1]
                       :coeffect user}
                      {:effect   [:persistence.stream/create stream]
                       :coeffect persisted}
                      {:final-effect [::new/->processed persisted]}]
        continuation (e/continuation new/process)]
    (script/test continuation script)))
