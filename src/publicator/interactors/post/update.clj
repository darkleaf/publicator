(ns publicator.interactors.post.update
  (:require
   [publicator.domain.post :as post]
   [publicator.interactors.services.user-session :as user-session]
   [publicator.interactors.abstractions.storage :as storage]
   [better-cond.core :as b]
   [clojure.spec.alpha :as s]))

(s/def ::params (s/keys :req-un [::post/title ::post/content]))

(defn- check-logged-in []
  (when (user-session/logged-out?)
    {:type ::logged-out}))

(defn- check-params [params]
  (when-let [exp (s/explain-data ::params params)]
    {:type         ::invalid-params
     :explain-data exp}))

(defn- check-authorization [post]
  (when-not (post/author? post (user-session/user))
    {:type ::not-authorized}))

(defn- update-post [post params]
  (let [params (select-keys params [:title :content])]
    (storage/swap! post merge params)))

(defn- params-for-update [post]
  (select-keys post [:title :content]))

(defn initial-params [id]
  (or
   (check-logged-in)
   (storage/with-tx t
     (b/cond
       :let [post (storage/get-one t id)]
       (nil? post) {:type ::not-found}
       :let [err (check-authorization @post)]
       (some? err) err
       :let [params (params-for-update @post)]
       {:type ::initial-params
        :initial-params params}))))

(defn process [id params]
  (or
   (check-logged-in)
   (check-params params)
   (storage/with-tx t
     (b/cond
       :let [post (storage/get-one t id)]
       (nil? post) {:type ::not-found}
       :let [err (check-authorization @post)]
       (some? err) err
       :do (update-post post params)
       {:type ::processed
        :post @post}))))
