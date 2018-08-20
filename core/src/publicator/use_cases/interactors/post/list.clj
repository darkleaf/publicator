(ns publicator.use-cases.interactors.post.list
  (:require
   [publicator.use-cases.services.user-session :as user-session]
   [publicator.use-cases.abstractions.post-queries :as post-q]
   [clojure.spec.alpha :as s]))

(defn process []
  (let [user  (user-session/user)
        posts (post-q/get-list)]
    [::processed posts]))

(s/def ::processed (s/tuple #{::processed} ::post-q/posts))

(s/fdef process
  :args nil?
  :ret (s/or :ok ::processed))
