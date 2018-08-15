(ns publicator.web.routing
  (:require
   [sibiro.core]
   [sibiro.extras]
   [clojure.set :as set]
   [publicator.web.controllers.pages.root :as pages.root]
   [publicator.web.controllers.user.log-in :as user.log-in]
   [publicator.web.controllers.user.log-out :as user.log-out]
   [publicator.web.controllers.user.register :as user.register]
   [publicator.web.controllers.post.list :as post.list]
   [publicator.web.controllers.post.show :as post.show]
   [publicator.web.controllers.post.create :as post.create]
   [publicator.web.controllers.post.update :as post.update]))

;; todo: auto load, clojure tools namespace

(def routes
  (sibiro.core/compile-routes
   (set/union
    pages.root/routes
    user.log-in/routes
    user.log-out/routes
    user.register/routes
    post.list/routes
    post.show/routes
    post.create/routes
    post.update/routes)))

(def handler (sibiro.extras/make-handler routes))

(defn uri-for [& args]
  (let [ret (apply sibiro.core/uri-for routes args)]
    (assert (some? ret) (str "route not found for " args))
    ret))

(defn path-for [& args]
  (let [ret (apply sibiro.core/path-for routes args)]
    (assert (some? ret) (str "route not found for " args))
    ret))
