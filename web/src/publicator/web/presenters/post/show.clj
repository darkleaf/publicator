(ns publicator.web.presenters.post.show
  (:require
   [publicator.domain.aggregates.user :as user]))

(defn processed [post]
  {:title          (:title post)
   :content        (:content post)
   :user-full-name (::user/full-name post)})
