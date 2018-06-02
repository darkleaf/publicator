(ns publicator.web.presenters.layout-test
  (:require
   [clojure.test :as t]
   [publicator.web.presenters.layout :as sut]
   [publicator.use-cases.test.fakes :as fakes]
   [publicator.use-cases.test.factories :as factories]
   [publicator.use-cases.services.user-session :as user-session]))

(t/use-fixtures :each fakes/fixture)

(t/deftest logged-in
  (let [user (factories/create-user)
        _    (user-session/log-in! user)
        req  {} ;; stub
        resp (sut/present req)]
    (t/is (not (contains? resp :log-in)))
    (t/is (not (contains? resp :register)))
    (t/is (contains? resp :log-out))))

(t/deftest logged-out
  (let [req  {} ;; stub
        resp (sut/present req)]
    (t/is (contains? resp :log-in))
    (t/is (contains? resp :register))
    (t/is (not (contains? resp :log-out)))))
