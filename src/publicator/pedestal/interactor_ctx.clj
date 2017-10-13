(ns publicator.pedestal.interactor-ctx)

(defn build [ctx]
  {:name  ::interactor-ctx
   :enter (fn [context]
            (update-in context [:request :interactor-ctx] merge ctx))})
