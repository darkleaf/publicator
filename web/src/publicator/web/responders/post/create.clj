(ns publicator.web.responders.post.create
  (:require
   [publicator.use-cases.interactors.post.create :as interactor]
   [publicator.web.responders.base :as base]
   [publicator.web.responders.responses :as responses]
   [publicator.web.forms.post.params :as form]
   [publicator.web.routing :as routing]))

(defmethod base/->resp ::interactor/initial-params [[_ params] _]
  (let [cfg  {:url    (routing/path-for :post.create/process)
              :method :post}
        form (form/build cfg params)]
    (responses/render-form form)))

(derive ::interactor/processed ::base/redirect-to-root)
(derive ::interactor/invalid-params ::base/invalid-params)
(derive ::interactor/logged-out ::base/forbidden)
