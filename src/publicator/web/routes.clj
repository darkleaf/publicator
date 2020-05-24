(ns publicator.web.routes
  (:require
   [publicator.core.use-cases.interactors.user.list :as user.list]
   [publicator.core.use-cases.interactors.user.log-in :as user.log-in]
   [publicator.core.use-cases.interactors.user.log-out :as user.log-out]
   [publicator.core.use-cases.interactors.user.register :as user.register]
   [publicator.core.use-cases.interactors.user.update :as user.update]))

(defn routes []
  [["/user"
    ["/log-in" {:name :user/log-in
                :get  (fn [req] [user.log-in/form])
                :post (fn [req] [user.log-in/process (:body req)])}]
    ["/log-out" {:name :user/log-out
                 :post (fn [req] [user.log-out/process])}]
    ["/register" {:name :user/register
                  :get  (fn [req] [user.register/form])
                  :post (fn [req] [user.register/process (:body req)])}]
    ["/list" {:name :user/list
              :get  (fn [req] [user.list/process])}]
    ;; TODO: add a coersion
    ["/update/:id" {:name :user/update
                    :get  (fn [req] [user.update/form (-> req :path-params :id Integer.)])
                    :post (fn [req] [user.update/process (-> req :path-params :id Integer.) (:body req)])}]]])
