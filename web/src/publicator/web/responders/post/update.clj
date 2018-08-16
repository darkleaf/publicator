(ns publicator.web.responders.post.update
  (:require
   [publicator.use-cases.interactors.post.update :as interactor]
   [publicator.web.responders.base :as responders.base]
   [publicator.web.responses :as responses]
   [publicator.web.forms.post.params :as form]
   [publicator.web.routing :as routing]))

(defmethod responders.base/result->resp ::interactor/initial-params [[_ params] [id]]
  (let [cfg  {:url    (routing/path-for :post.update/process {:id (str id)})
              :method :post}
        form (form/build cfg params)]
    (responses/render-form form)))

(derive ::interactor/processed ::responders.base/redirect-to-root)
(derive ::interactor/invalid-params ::responders.base/invalid-params)
(derive ::interactor/logged-out ::responders.base/forbidden)
(derive ::interactor/not-authorized ::responders.base/forbidden)
(derive ::interactor/not-found ::responders.base/not-found)
