(ns publicator.web.responders.post.list
  (:require
   [publicator.use-cases.interactors.post.list :as interactor]
   [publicator.web.responders.base :as base]
   [publicator.web.responders.responses :as responses]
   [publicator.web.presenters.post.list :as presenter]))

(defmethod base/->resp ::interactor/processed [[_ posts] _]
  (let [model (presenter/processed posts)]
    (responses/render-page "post/list" model)))
