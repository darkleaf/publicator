(ns publicator.core.use-cases.stream.new-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.use-cases.stream.new :as new]
   [clojure.test :as t]
   [darkleaf.effect.core :as e]
   [darkleaf.effect.script :as script]
   [datascript.core :as d]))

(t/deftest process-success
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {:current-user-id 1}}
                      {:effect   [:persistence.user/get-by-id 1]
                       :coeffect (-> (agg/allocate)
                                     (d/db-with [{:db/ident             :root
                                                  :agg/id               1
                                                  :user/login           "admin"
                                                  :user/password-digest "digest"
                                                  :user/state           :active
                                                  :user/role            :admin}]))}
                      {:effect   [:ui.form/edit (agg/allocate)]
                       :coeffect [{:stream.translation/stream :root
                                   :stream.translation/lang   :en
                                   :stream.translation/name   "Stream"}
                                  {:stream.translation/stream :root
                                   :stream.translation/lang   :ru
                                   :stream.translation/name   "Поток"}]}
                      {:effect   [:persistence.stream/next-id]
                       :coeffect 42}
                      {:effect   [:persistence/create
                                  (-> (agg/allocate)
                                      (d/db-with [{:db/ident     :root
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
