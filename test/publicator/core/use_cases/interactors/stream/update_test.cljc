(ns publicator.core.use-cases.interactors.stream.update-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.use-cases.interactors.stream.update :as update]
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
        stream       (agg/allocate {:db/ident     :root
                                    :agg/id       1
                                    :stream/state :active}
                                   {:stream.translation/stream :root
                                    :stream.translation/lang   :en
                                    :stream.translation/name   "Stream"}
                                   {:stream.translation/stream :root
                                    :stream.translation/lang   :ru
                                    :stream.translation/name   "Поток"})
        form         (agg/allocate {:db/ident     :root
                                    :agg/id       1
                                    :stream/state :active}
                                   {:stream.translation/stream :root
                                    :stream.translation/lang   :en
                                    :stream.translation/name   "Stream"}
                                   {:stream.translation/stream :root
                                    :stream.translation/lang   :ru
                                    :stream.translation/name   "Поток"})
        script       [{:args [1]}
                      {:effect   [:persistence.stream/get-by-id 1]
                       :coeffect stream}
                      {:effect   [:session/get]
                       :coeffect session}
                      {:effect   [:persistence.user/get-by-id 1]
                       :coeffect user}
                      {:final-effect [::update/->form form]}]
        continuation (e/continuation update/form)]
    (script/test continuation script)))

(t/deftest process-success
  (let [session      {::user-session/id 1}
        user         (agg/allocate {:db/ident             :root
                                    :agg/id               1
                                    :user/login           "admin"
                                    :user/password-digest "digest"
                                    :user/state           :active
                                    :user/role            :admin})
        stream       (agg/allocate {:db/ident     :root
                                    :agg/id       1
                                    :stream/state :active}
                                   {:stream.translation/stream :root
                                    :stream.translation/lang   :en
                                    :stream.translation/name   "Stream"}
                                   {:stream.translation/stream :root
                                    :stream.translation/lang   :ru
                                    :stream.translation/name   "Поток"})
        form         (agg/allocate {:db/ident     :root
                                    :agg/id       1
                                    :stream/state :active}
                                   {:stream.translation/stream :root
                                    :stream.translation/lang   :en
                                    :stream.translation/name   "New Stream"}
                                   {:stream.translation/stream :root
                                    :stream.translation/lang   :ru
                                    :stream.translation/name   "Новый Поток"})
        updated      (agg/allocate {:db/ident     :root
                                    :agg/id       1
                                    :stream/state :active}
                                   {:stream.translation/stream :root
                                    :stream.translation/lang   :en
                                    :stream.translation/name   "New Stream"}
                                   {:stream.translation/stream :root
                                    :stream.translation/lang   :ru
                                    :stream.translation/name   "Новый Поток"})
        script       [{:args [form]}
                      {:effect   [:persistence.stream/get-by-id 1]
                       :coeffect stream}
                      {:effect   [:session/get]
                       :coeffect session}
                      {:effect   [:persistence.user/get-by-id 1]
                       :coeffect user}
                      {:effect   [:persistence.stream/update updated]
                       :coeffect updated}
                      {:final-effect [::update/->processed updated]}]
        continuation (e/continuation update/process)]
    (script/test continuation script)))
