(ns publicator.interactors.post.show
  (:require
   [better-cond.core :as b]
   [publicator.interactors.abstractions.storage :as storage]))

(b/defnc process [id]
  :let [post (storage/tx-get-one id)]
  (nil? post) {:type ::not-found}
  {:type ::processed
   :post post})
