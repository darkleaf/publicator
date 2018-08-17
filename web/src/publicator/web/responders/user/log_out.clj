(ns publicator.web.responders.user.log-out
  (:require
   [publicator.use-cases.interactors.user.log-out :as interactor]
   [publicator.web.responders.base :as responders.base]
   [publicator.web.responses :as responses]
   [publicator.web.routing :as routing]))

(defmethod responders.base/result->resp ::interactor/processed [_]
  (responses/redirect-for-page (routing/path-for :pages/root)))

(derive ::interactor/already-logged-out ::responders.base/forbidden)
