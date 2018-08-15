(ns publicator.web.routing
  (:require
   [sibiro.core]
   [sibiro.extras]
   [publicator.web.controllers.routes :as routes]))

(def routes (sibiro.core/compile-routes routes/routes))

(def handler (sibiro.extras/make-handler routes))

(defn uri-for [& args]
  (let [ret (apply sibiro.core/uri-for routes args)]
    (assert (some? ret) (str "route not found for " args))
    ret))

(defn path-for [& args]
  (let [ret (apply sibiro.core/path-for routes args)]
    (assert (some? ret) (str "route not found for " args))
    ret))
