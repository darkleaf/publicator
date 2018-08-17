(ns publicator.web.responders.post.create
  (:require
   [publicator.use-cases.interactors.post.create :as interactor]
   [publicator.web.responders.base :as responders.base]
   [publicator.web.responses :as responses]
   [publicator.web.forms.post.params :as form]
   [publicator.web.routing :as routing]))

(defmethod responders.base/result->resp ::interactor/initial-params [[_ params]]
  (let [cfg  {:url    (routing/path-for :post.create/process)
              :method :post}
        form (form/build cfg params)]
    (responses/render-form form)))

(derive ::interactor/processed ::responders.base/redirect-to-root)
(derive ::interactor/invalid-params ::responders.base/invalid-params)
(derive ::interactor/logged-out ::responders.base/forbidden)
