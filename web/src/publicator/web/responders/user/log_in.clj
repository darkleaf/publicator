(ns publicator.web.responders.user.log-in
  (:require
   [publicator.use-cases.interactors.user.log-in :as interactor]
   [publicator.web.responders.base :as responders.base]
   [publicator.web.responses :as responses]
   [publicator.web.forms.user.log-in :as form]
   [publicator.web.routing :as routing]))

(defmethod responders.base/result->resp ::interactor/initial-params [[_ params]]
  (let [form (form/build params)]
    (responses/render-form form)))

(defmethod responders.base/result->resp ::interactor/authentication-failed [_]
  (-> (form/authentication-failed-error)
      responses/render-errors))

(derive ::interactor/processed ::responders.base/redirect-to-root)
(derive ::interactor/invalid-params ::responders.base/invalid-params)
(derive ::interactor/already-logged-in ::responders.base/forbidden)
