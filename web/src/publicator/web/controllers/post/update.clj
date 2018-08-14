(ns publicator.web.controllers.post.update
  (:require
   [publicator.use-cases.interactors.post.update :as interactor]))
   ;; [publicator.web.presenters.explain-data :as explain-data]
   ;; [publicator.web.forms.post.params :as form]
   ;; [publicator.web.controllers.base :as base]
   ;; [publicator.web.url-helpers :as url-helpers]))

(defn- req->id [req]
  (-> req
      :route-params
      :id
      Integer.))

(defn initial-params [req]
  (let [id (req->id req)]
    [interactor/initial-params id]))

(defn process [{:keys [transit-params] :as req}]
  (let [id (req->id req)]
    [interactor/process id transit-params]))

;; (defmethod base/handle ::interactor/initial-params [ctx [_ params]]
;;   (let [cfg  {:url    (url-helpers/path-for :post.update/handler {:id (-> ctx :id str)})
;;               :method :post}
;;         form (form/build cfg params)]
;;     (base/render-form form)))

;; (defmethod base/handle ::interactor/processed [_ _]
;;   (base/redirect-form (url-helpers/path-for :pages/root)))

;; (defmethod base/handle ::interactor/invalid-params [_ [_ explain-data]]
;;   (-> explain-data
;;       explain-data/->errors
;;       base/errors))

;; (derive ::interactor/logged-out ::base/forbidden)
;; (derive ::interactor/not-authorized ::base/forbidden)
;; (derive ::interactor/not-found ::base/not-found)

(def routes
  #{[:get "/posts/:id{\\d+}/edit" #'initial-params :post.update/initial-params]
    [:post "/posts/:id{\\d+}/edit" #'process :post.update/process]})
