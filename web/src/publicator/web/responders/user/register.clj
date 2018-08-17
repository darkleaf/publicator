(ns publicator.web.responders.user.register
  (:require
   [publicator.use-cases.interactors.user.register :as interactor]
   [publicator.web.responders.base :as responders.base]
   [publicator.web.responses :as responses]
   [publicator.web.presenters.explain-data :as explain-data]
   [publicator.web.forms.user.register :as form]
   [publicator.web.routing :as routing]))

(defmethod responders.base/result->resp ::interactor/initial-params [[_ params]]
  (let [form (form/build params)]
    (responses/render-form form)))

(derive ::interactor/processed ::responders.base/redirect-to-root)
(derive ::interactor/invalid-params ::responders.base/invalid-params)
(derive ::interactor/already-logged-in ::responders.base/forbidden)
(derive ::interactor/already-registered ::responders.base/forbidden)
