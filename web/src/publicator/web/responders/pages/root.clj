(ns publicator.web.responders.pages.root
  (:require
   [publicator.web.responders.base :as base]
   [publicator.web.responders.responses :as responses]))

(defmethod base/->resp :pages/root [_ _]
  (responses/render-page "pages/root" {}))
