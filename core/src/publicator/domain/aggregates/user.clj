(ns publicator.domain.aggregates.user
  (:require
   [publicator.domain.abstractions.id-generator :as id-generator]
   [publicator.domain.abstractions.instant :as instant]
   [publicator.domain.abstractions.password-hasher :as password-hasher]
   [publicator.domain.aggregate :as agg]
   [publicator.utils.datascript.validation :as d.validation]
   [publicator.utils.string :as u.str]))

(def ^:const states #{:active :archived})

(defn- hash-password [user]
  (let [password        (-> user agg/root :user/password)
        password-digest (password-hasher/*derive* password)]
    [[:db/retract :root :user/password password]
     [:db/add :root :user/password-digest password-digest]]))

(def spec
  {:type          :user
   :defaults-tx   (fn [] [[:db/add :root :root/id (id-generator/*generate* :user)]
                          [:db/add :root :user/state :active]])
   :additional-tx (fn [] [[:db.fn/call hash-password]])
   :validator     (d.validation/compose
                   (d.validation/attributes [:user/login string?]
                                            [:user/login u.str/match? #"\w{3,256}"]
                                            [:user/password string?]
                                            [:user/password u.str/match? #".{8,256}"]
                                            [:user/password-digest string?]
                                            [:user/state states])
                   (d.validation/in-case-of agg/root-q
                                            [:user/login not-empty]
                                            [:user/password-digest not-empty]
                                            [:user/state some?]))})
