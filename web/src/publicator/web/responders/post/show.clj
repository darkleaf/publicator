(ns publicator.web.responders.post.show
  (:require
   [publicator.use-cases.interactors.post.show :as interactor]
   [publicator.web.responders.base :as responders.base]
   [publicator.web.responses :as responses]
   [publicator.web.presenters.post.show :as presenter]))

(defmethod responders.base/result->resp ::interactor/processed [[_ posts]]
  (let [model (presenter/processed posts)]
    (responses/render-page "post/show" model)))

(derive ::interactor/not-found ::responders.base/not-found)
