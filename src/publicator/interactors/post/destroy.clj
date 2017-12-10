(ns publicator.interactors.post.destroy
  (:require
   [publicator.domain.post :as post]
   [publicator.interactors.services.user-session :as user-session]
   [publicator.interactors.abstractions.storage :as storage]
   [better-cond.core :as b]))

(defn- check-logged-in []
  (when (user-session/logged-out?)
    {:type ::logged-out}))

(defn- check-authorization [post]
  (when-not (post/author? post (user-session/user))
    {:type ::not-authorized}))

(defn process [id]
  (or
   (check-logged-in)
   (storage/with-tx t
     (b/cond
       :let [post (storage/get-one t id)]
       (nil? post) {:type ::not-found}
       :let [err (check-authorization @post)]
       (some? err) err
       :do (storage/destroy! post)
       {:type ::processed}))))
