(ns publicator.domain.aggregates.user
  (:require
   [publicator.domain.abstractions.id-generator :as id-generator]
   [publicator.domain.abstractions.password-hasher :as password-hasher]
   [publicator.domain.aggregate :as agg]
   [publicator.utils.datascript.validation :as d.validation]
   [publicator.domain.validators.uniqueness :as uniqueness]
   [publicator.utils.string :as u.str]))

(def states #{:active :archived})

(def spec
  {:type         :user
   :id-generator #(id-generator/*generate* :user)
   :transformer  (fn [user]
                   (when-some [password (-> user agg/root :user/password)]
                     [[:db/add :root :user/password-digest (password-hasher/*derive* password)]]))
   :validator    (d.validation/compose
                  (d.validation/predicate [[:user/login string?]
                                           [:user/login u.str/match? #"\w{3,256}"]
                                           [:user/password string?]
                                           [:user/password u.str/match? #".{8,256}"]
                                           [:user/password-digest string?]
                                           [:user/password-digest not-empty]
                                           [:user/state states]])

                  (d.validation/required agg/root-q
                                         #{:user/login
                                           :user/password-digest
                                           :user/state})

                  (d.validation/required '{:find  [[?e ...]]
                                           :where [[?e :db/ident :root]
                                                   [(missing? $ ?e :user/password-digest)]]}
                                          #{:user/password})

                  (uniqueness/validator #{:user/login}))})

;; todo:
(defn authenticated? [user password])
