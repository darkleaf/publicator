(ns publicator.web.controllers.pages.root
  (:require
   [publicator.web.responses :as responses]))

(defn show [_]
  (responses/render-page "pages/root" {}))

(def routes
  #{[:get "/" #'show :pages/root]})
