(ns publicator.web.controllers.pages.root)

(defn- interactor []
  [:pages/root])

(defn show [req]
  [interactor])

(def routes
  #{[:get "/" #'show :pages/root]})
