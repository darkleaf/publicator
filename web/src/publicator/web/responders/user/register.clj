(ns publicator.web.responders.user.register
  (:require
   [publicator.use-cases.interactors.user.register :as interactor]
   [publicator.web.responders.base :as base]
   [publicator.web.responses :as responses]
   [publicator.web.presenters.explain-data :as explain-data]
   [publicator.web.forms.user.register :as form]
   [publicator.web.routing :as routing]))

(defmethod base/->resp ::interactor/initial-params [[_ params] _]
  (let [form (form/build params)]
    (responses/render-form form)))

(derive ::interactor/processed ::base/redirect-to-root)
(derive ::interactor/invalid-params ::base/invalid-params)
(derive ::interactor/already-logged-in ::base/forbidden)
(derive ::interactor/already-registered ::base/forbidden)
