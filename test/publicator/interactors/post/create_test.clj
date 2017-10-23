(ns publicator.interactors.post.create-test
  (:require
   [publicator.interactors.post.create :as sut]
   [publicator.interactors.abstractions.storage :as storage]
   [publicator.interactors.abstractions.session :as session]
   [publicator.domain.post :as post]
   [publicator.domain.user :as user]
   [publicator.interactors.fixtures :as fixtures]
   [clojure.test :as t]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]))

(t/use-fixtures :each fixtures/all)

;; ~~~~ move ~~~~
(defn create-user []
  (let [params (sgen/generate (s/gen ::user/build-params))]
    (storage/tx-create (user/build params))))

(defn log-in! [user]
  (session/log-in! (:id user)))
;; ~~~~ /move ~~~


(t/deftest process
  (let [user   (create-user)
        _      (log-in! user)
        params (sgen/generate (s/gen ::sut/params))
        resp   (sut/process params)
        post   (:post resp)]
    (t/testing "success"
      (t/is (= (:type resp) ::sut/processed))
      (t/is (some? post)))
    (t/testing "assign author"
      (t/is (= (:author-id post)
               (:id user))))
    (t/testing "persisted"
      (t/is (some? (storage/tx-get-one (:id post)))))))

(t/deftest logged-out
  (let [params (sgen/generate (s/gen ::sut/params))
        resp   (sut/process params)]
    (t/testing "has error"
      (t/is (=  (:type resp)
                ::sut/logged-out)))))

(t/deftest invalid-params
  (let [user   (create-user)
        _      (log-in! user)
        params {}
        resp   (sut/process params)]
    (t/testing "error"
      (t/is (= (:type resp) ::sut/invalid-params))
      (t/is (contains? resp  :explain-data)))))
