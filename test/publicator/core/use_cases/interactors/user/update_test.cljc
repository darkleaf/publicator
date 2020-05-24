(ns publicator.core.use-cases.interactors.user.update-test
  (:require
   [clojure.test :as t]
   [darkleaf.effect.core :as e]
   [darkleaf.effect.middleware.contract :as contract]
   [darkleaf.effect.script :as script]
   [datascript.core :as d]
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.use-cases.contracts :as contracts]
   [publicator.core.use-cases.interactors.user.update :as update]
   [publicator.core.use-cases.services.user-session :as user-session]))

(t/deftest form-success
  (let [user-id      1
        user         (agg/build {:db/ident             :root
                                 :agg/id               user-id
                                 :user/login           "john"
                                 :user/password-digest "digest"
                                 :user/state           :active})
        form         (agg/build {:db/ident   :root
                                 :user/login "john"
                                 :user/state :active})
        script       [{:args [user-id]}
                      {:effect   [:persistence.user/get-by-id user-id]
                       :coeffect user}
                      {:effect   [:session/get]
                       :coeffect {::user-session/id user-id}}
                      {:effect   [:persistence.user/get-by-id user-id]
                       :coeffect user}
                      {:final-effect [::update/->form form]}]
        continuation (-> update/form
                         (e/continuation)
                         (contract/wrap-contract @contracts/registry `update/form))]
    (script/test continuation script)))

(t/deftest form-not-authorized
  (let [user-id       1
        user          (agg/build {:db/ident             :root
                                  :agg/id               user-id
                                  :user/login           "john"
                                  :user/password-digest "digest"
                                  :user/state           :active})
        other-user-id 2
        other-user    (agg/build {:db/ident             :root
                                  :agg/id               other-user-id
                                  :user/login           "other-john"
                                  :user/password-digest "digest"
                                  :user/state           :active})
        script        [{:args [other-user-id]}
                       {:effect   [:persistence.user/get-by-id other-user-id]
                        :coeffect other-user}
                       {:effect   [:session/get]
                        :coeffect {::user-session/id user-id}}
                       {:effect   [:persistence.user/get-by-id user-id]
                        :coeffect user}
                       {:final-effect [::update/->not-authorized]}]
        continuation  (-> update/form
                          (e/continuation)
                          (contract/wrap-contract @contracts/registry `update/form))]
    (script/test continuation script)))

(t/deftest form-user-not-found
  (let [user-id      1
        script       [{:args [user-id]}
                      {:effect   [:persistence.user/get-by-id user-id]
                       :coeffect nil}
                      {:final-effect [::update/->user-not-found]}]
        continuation (-> update/form
                         (e/continuation)
                         (contract/wrap-contract @contracts/registry `update/form))]
    (script/test continuation script)))

(t/deftest process-success
  (let [user-id   1
        form      (agg/build {:db/ident      :root
                              :user/login    "john"
                              :user/password "new password"
                              :user/state    :active
                              :user/author?  true}
                             {:author.translation/author     :root
                              :author.translation/lang       :en
                              :author.translation/first-name "John"
                              :author.translation/last-name  "Doe"}
                             {:author.translation/author     :root
                              :author.translation/lang       :ru
                              :author.translation/first-name "Иван"
                              :author.translation/last-name  "Иванов"})
        user      (agg/build {:db/ident             :root
                              :agg/id               user-id
                              :user/login           "john"
                              :user/password-digest "digest"
                              :user/state           :active})
        persisted (agg/build {:db/ident             :root
                              :agg/id               user-id
                              :user/login           "john"
                              :user/password-digest "new digest"
                              :user/state           :active
                              :user/author?         true}
                             {:author.translation/author     :root
                              :author.translation/lang       :en
                              :author.translation/first-name "John"
                              :author.translation/last-name  "Doe"}
                             {:author.translation/author     :root
                              :author.translation/lang       :ru
                              :author.translation/first-name "Иван"
                              :author.translation/last-name  "Иванов"})
        script    [{:args [user-id form]}
                   {:effect   [:persistence.user/get-by-id user-id]
                    :coeffect user}

                   {:effect   [:session/get]
                    :coeffect {::user-session/id user-id}}
                   {:effect   [:persistence.user/get-by-id user-id]
                    :coeffect user}

                   {:effect   [:session/get]
                    :coeffect {::user-session/id user-id}}
                   {:effect   [:persistence.user/get-by-id user-id]
                    :coeffect user}

                   {:effect   [:hasher/derive "new password"]
                    :coeffect "new digest"}

                   {:effect   [:persistence.user/update persisted]
                    :coeffect persisted}
                   {:final-effect [::update/->processed persisted]}]
        continuation (-> update/process
                         (e/continuation)
                         (contract/wrap-contract @contracts/registry `update/process))]
    (script/test continuation script)))

(t/deftest process-invalid-form
  (let [form         (agg/build {:db/ident :root})
        with-errors  (agg/build #:error{:attr   :user/login
                                        :entity :root
                                        :type   :required}
                                #:error{:attr   :user/state
                                        :entity :root
                                        :type   :required})
        script       [{:args [1 form]}
                      {:final-effect [::update/->invalid-form with-errors]}]
        continuation (-> update/process
                         (e/continuation)
                         (contract/wrap-contract @contracts/registry `update/process))]
    (script/test continuation script)))
