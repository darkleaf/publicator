(ns publicator.web.responders.post.update
  (:require
   [publicator.use-cases.interactors.post.update :as interactor]
   [publicator.web.responders.base :as base]
   [publicator.web.responders.responses :as responses]
   [publicator.web.presenters.explain-data :as explain-data]
   [publicator.web.forms.post.params :as form]
   [publicator.web.routing :as routing]))

(defmethod base/->resp ::interactor/initial-params [[_ params] [id]]
  (let [cfg  {:url    (routing/path-for :post.update/handler {:id (str id)})
              :method :post}
        form (form/build cfg params)]
    (responses/render-form form)))

(defmethod base/->resp ::interactor/invalid-params [[_ explain-data] _]
  (-> explain-data
      explain-data/->errors
      responses/render-errors))

(defmethod base/->resp ::interactor/processed [_ _]
  (responses/redirect-for-form (routing/path-for :pages/root)))

(derive ::interactor/logged-out ::base/forbidden)
(derive ::interactor/not-authorized ::base/forbidden)
(derive ::interactor/not-found ::base/not-found)
