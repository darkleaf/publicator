(ns publicator.persistence.handlers-test
  (:require
   [cljc.java-time.instant :as time.instant]
   [clojure.test :as t]
   [darkleaf.effect.core :refer [effect]]
   [darkleaf.generator.core :refer [generator yield]]
   [datascript.core :as d]
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.publication :as publication]
   [publicator.core.use-cases.aggregates.user :as user]
   [publicator.persistence.test-system :as test-system]))

(def timestamptz (time.instant/parse "2020-01-01T00:00:00Z"))

(t/deftest user-get-by-id
  (let [f* (fn [id]
             (generator
               (yield (effect :persistence.user/get-by-id id))))]
    (t/testing "not-found"
      (t/is (= nil (test-system/run f* 42))))
    (t/testing "fixture"
      (let [user (-> (agg/build {:db/ident             :root
                                 :agg/id               -1
                                 :user/state           :active
                                 :user/admin?          true
                                 :user/author?         true
                                 :user/login           "admin"
                                 :user/password-digest "digest"}
                                {:translation/root              :root
                                 :translation/lang              :en
                                 :author.translation/first-name "John"
                                 :author.translation/last-name  "Doe"}
                                {:translation/root              :root
                                 :translation/lang              :ru
                                 :author.translation/first-name "Иван"
                                 :author.translation/last-name  "Иванов"}
                                {:author.achivement/root        :root
                                 :author.achivement/kind        :star
                                 :author.achivement/assigner-id -1})
                     (user/validate)
                     (agg/check-errors))]
        (t/is (= user (test-system/run f* -1)))))))

(t/deftest publication-get-by-id
  (let [f* (fn [id]
             (generator
               (yield (effect :persistence.publication/get-by-id id))))]
    (t/testing "not-found"
      (t/is (= nil (test-system/run f* 42))))
    (t/testing "article"
      (let [user (-> (agg/build {:db/ident               :root
                                 :agg/id                 -1
                                 :publication/type       :article
                                 :publication/state      :active
                                 :publication/author-id  -1
                                 :publication/related-id #{-1}
                                 :article/image-url      "cat.png"}
                                {:translation/root                     :root
                                 :translation/lang                     :en
                                 :publication.translation/state        :published
                                 :publication.translation/title        "Funny cat"
                                 :publication.translation/summary      "summary"
                                 :publication.translation/published-at timestamptz
                                 :publication.translation/tag          #{"cat"}
                                 :article.translation/content          "text"}
                                {:translation/root                     :root
                                 :translation/lang                     :ru
                                 :publication.translation/state        :published
                                 :publication.translation/title        "Забавный кот"
                                 :publication.translation/summary      "описание"
                                 :publication.translation/published-at timestamptz
                                 :publication.translation/tag          #{"кот"}
                                 :article.translation/content          "текст"})
                     (publication/validate)
                     (agg/check-errors))]
        (t/is (= user (test-system/run f* -1)))))
    (t/testing "gallery"
      (let [user (-> (agg/build {:db/ident               :root
                                 :agg/id                 -2
                                 :publication/type       :gallery
                                 :publication/state      :active
                                 :publication/author-id  -1
                                 :publication/related-id #{-2}
                                 :gallery/image-url      #{"cat-1.png" "cat-2.png"}}
                                {:translation/root                     :root
                                 :translation/lang                     :en
                                 :publication.translation/state        :published
                                 :publication.translation/title        "Funny cat"
                                 :publication.translation/summary      "summary"
                                 :publication.translation/published-at timestamptz
                                 :publication.translation/tag          #{"cat"}}
                                {:translation/root                     :root
                                 :translation/lang                     :ru
                                 :publication.translation/state        :published
                                 :publication.translation/title        "Забавный кот"
                                 :publication.translation/summary      "описание"
                                 :publication.translation/published-at timestamptz
                                 :publication.translation/tag          #{"кот"}})
                     (publication/validate)
                     (agg/check-errors))]
        (t/is (= user (test-system/run f* -2)))))))


;; (t/deftest user-create
;;   (let [user              (agg/build {:db/ident             :root
;;                                       :user/state           "active"
;;                                       :user/admin?          true
;;                                       :user/author?         true
;;                                       :user/login           "admin"
;;                                       :user/password-digest "digest"}
;;                                      {:author.translation/author     :root
;;                                       :author.translation/lang       "en"
;;                                       :author.translation/first-name "John"
;;                                       :author.translation/last-name  "Doe"}
;;                                      {:author.translation/author     :root
;;                                       :author.translation/lang       "ru"
;;                                       :author.translation/first-name "Иван"
;;                                       :author.translation/last-name  "Иванов"})
;;         ef                (fn [user]
;;                             (with-effects
;;                               (let [saved     (! (effect :persistence.user/create user))
;;                                     id        (agg/val-in saved :root :agg/id)
;;                                     refreshed (! (effect :persistence.user/get-by-id id))]
;;                                 [saved refreshed])))
;;         [saved refreshed] (run ef user)]
;;     (t/is (= saved refreshed))
;;     (t/is (= user
;;              (d/db-with saved     [[:db.fn/retractAttribute :root :agg/id]])
;;              (d/db-with refreshed [[:db.fn/retractAttribute :root :agg/id]])))))
