(ns publicator.pedestal.routes
  (:require
   [io.pedestal.http.route :as route]
   [publicator.web.pages.root.controller :as pages.root]
   [publicator.web.user.register.controller :as user.register]
   [publicator.web.user.log-in.controller :as user.log-in]
   [publicator.web.user.log-out.controller :as user.log-out]
   [publicator.web.post.create.controller :as post.create]
   [publicator.web.post.list.controller :as post.list]
   [publicator.web.post.show.controller :as post.show]
   [publicator.web.post.update.controller :as post.update]
   [publicator.web.post.destroy.controller :as post.destroy]))

(defn build []
  (route/expand-routes
   (reduce into [(pages.root/routes)
                 (user.register/routes)
                 (user.log-in/routes)
                 (user.log-out/routes)
                 (post.create/routes)
                 (post.list/routes)
                 (post.show/routes)
                 (post.update/routes)
                 (post.destroy/routes)])))
