(ns dev.seed
  (:require
   [publicator.factories :as factories]
   [publicator.interactors.abstractions.post-queries :as post-q]))

(defn seed [bindig-map]
  (with-bindings bindig-map
    (let [admin (factories/create-user :login "admin"
                                       :password "12345678"
                                       :full-name "Admin")]
      (factories/create-post :author-id (:id admin))
      (factories/create-post))))
