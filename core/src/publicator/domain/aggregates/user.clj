(ns publicator.domain.aggregates.user
  (:require
   [publicator.domain.abstractions.id-generator :as id-generator]
   [publicator.domain.abstractions.instant :as instant]
   [publicator.domain.aggregate :as agg]
   [publicator.utils.datascript.validation :as d.validation]
   [publicator.domain.validators.uniqueness :as uniqueness]
   [publicator.utils.string :as u.str]))

(def states #{:active :archived})

(def spec
  {:type         :user
   :id-generator #(id-generator/*generate* :user)
   :validator    (d.validation/compose
                  (d.validation/predicate [[:user/login string?]
                                           [:user/login u.str/match? #"\w{3,256}"]
                                           [:user/password-digest string?]
                                           [:user/password-digest not-empty]
                                           [:user/state states]])

                  (d.validation/required agg/root-q
                                         #{:user/login
                                           :user/password-digest
                                           :user/state})

                  (uniqueness/validator #{:user/login}))})
