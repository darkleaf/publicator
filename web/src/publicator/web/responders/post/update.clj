(ns publicator.web.responders.post.update
  (:require
   [publicator.use-cases.interactors.post.update :as interactor]
   [publicator.web.responders.base :as base]
   [publicator.web.responders.responses :as responses]
   [publicator.web.forms.post.params :as form]
   [publicator.web.routing :as routing]))

(defmethod base/->resp ::interactor/initial-params [[_ params] [id]]
  (let [cfg  {:url    (routing/path-for :post.update/process {:id (str id)})
              :method :post}
        form (form/build cfg params)]
    (responses/render-form form)))

(derive ::interactor/processed ::base/redirect-to-root)
(derive ::interactor/invalid-params ::base/invalid-params)
(derive ::interactor/logged-out ::base/forbidden)
(derive ::interactor/not-authorized ::base/forbidden)
(derive ::interactor/not-found ::base/not-found)
