(ns publicator.web.responders.user.log-in
  (:require
   [publicator.use-cases.interactors.user.log-in :as interactor]
   [publicator.web.responders.base :as base]
   [publicator.web.responses :as responses]
   [publicator.web.forms.user.log-in :as form]
   [publicator.web.routing :as routing]))

(defmethod base/->resp ::interactor/initial-params [[_ params] _]
  (let [form (form/build params)]
    (responses/render-form form)))

(defmethod base/->resp ::interactor/authentication-failed [_ _]
  (-> (form/authentication-failed-error)
      responses/render-errors))

(derive ::interactor/processed ::base/redirect-to-root)
(derive ::interactor/invalid-params ::base/invalid-params)
(derive ::interactor/already-logged-in ::base/forbidden)
