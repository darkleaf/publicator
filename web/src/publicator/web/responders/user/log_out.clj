(ns publicator.web.responders.user.log-out
  (:require
   [publicator.use-cases.interactors.user.log-out :as interactor]
   [publicator.web.responders.base :as base]
   [publicator.web.responders.responses :as responses]
   [publicator.web.routing :as routing]))

(defmethod base/->resp ::interactor/processed [_ _]
  (responses/redirect-for-page (routing/path-for :pages/root)))

(derive ::interactor/already-logged-out ::base/forbidden)
