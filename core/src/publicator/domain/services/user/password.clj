(ns publicator.domain.services.user.password
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.abstractions.password-hasher :as password-hasher]
   [publicator.utils.datascript.validation :as d.validation]
   [publicator.utils.string :as u.str]))

(defn process [user]
  (if-some [password  (-> user agg/root :user/password)]
    (let [password-digest (password-hasher/*derive* password)]
      (agg/change user [[:db/add :root :user/password-digest password-digest]]
                  agg/allow-everething))
    user))

(def validator
  (d.validation/compose
   (d.validation/predicate [[:user/password string?]
                            [:user/password u.str/match? #".{8,256}"]])
   (d.validation/required '{:find  [[?e ...]]
                            :where [[?e :db/ident :root]
                                    [(missing? $ ?e :user/password-digest)]]}
                          #{:user/password})))

;; todo:
(defn authenticated? [user password])
