(ns publicator.use-cases.interactors.user.register-test)
;;   (:require
;;    [publicator.use-cases.interactors.user.register :as sut]
;;    [publicator.use-cases.services.user-session :as user-session]
;;    [publicator.use-cases.abstractions.storage :as storage]
;;    [publicator.use-cases.abstractions.scaffolding :as scaffolding]
;;    [publicator.domain.aggregate :as aggregate]
;;    [publicator.utils.test :as u.t]
;;    [clojure.test :as t]
;;    [datascript.core :as d]))

;; (defn test-process []
;;   (let [tx-data [[:db/add :root :user/login "john"]
;;                  [:db/add :root :user-form/password "pass"]]
;;         user   (sut/process tx-data)]
;;     (t/testing "success"
;;       (t/is (some? user)))
;;     (t/testing "logged in"
;;       (t/is (user-session/logged-in?)))
;;     (t/testing "persisted"
;;       (t/is (some? (storage/transaction
;;                     (storage/*get* :user (-> user aggregate/root :root/id))))))))

;; ;; (t/deftest already-registered
;; ;;   (let [params (factories/gen ::sut/params)
;; ;;         _      (factories/create-user {:login (:login params)})
;; ;;         [tag]  (sut/process params)]
;; ;;     (t/testing "has error"
;; ;;       (t/is (= ::sut/already-registered tag)))
;; ;;     (t/testing "not sign in"
;; ;;       (t/is (user-session/logged-out?)))))

;; ;; (t/deftest already-logged-in
;; ;;   (let [user   (factories/create-user)
;; ;;         _      (user-session/log-in! user)
;; ;;         params (factories/gen ::sut/params)
;; ;;         [tag]  (sut/process params)]
;; ;;     (t/testing "has error"
;; ;;       (t/is (= ::sut/already-logged-in tag)))))

;; ;; (t/deftest invalid-params
;; ;;   (let [params  {}
;; ;;         [tag _] (sut/process params)]
;; ;;     (t/testing "error"
;; ;;       (t/is (= ::sut/invalid-params tag)))))

;; (t/deftest register
;;   (u.t/run 'publicator.use-cases.interactors.user.register-test
;;     scaffolding/setup))
