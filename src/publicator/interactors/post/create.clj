(ns publicator.interactors.post.create
  (:require
   [publicator.interactors.abstractions.session :as session]
   [publicator.interactors.abstractions.storage :as storage]
   [publicator.domain.post :as post]
   [better-cond.core :as b]
   [clojure.spec.alpha :as s]))

(s/def ::params (s/keys :req-un [::post/title]))

(defn- check-logged-in []
  (when (session/logged-out?)
    {:type ::logged-out}))

(defn- check-params [params]
  (when-let [exp (s/explain-data ::params params)]
    {:type         ::invalid-params
     :explain-data exp}))

(defn- create-post [params]
  (let [params (assoc params :author-id (session/user-id))]
    (storage/tx-create (post/build params))))

(b/defnc initial-params []
  :let [err (check-logged-in)]
  (some? err) err
  {:type ::initial-params
   :initial-params {}})

(b/defnc process [params]
  :let [err (or
             (check-logged-in)
             (check-params params))]
  (some? err) err
  :let [post (create-post params)]
  {:type ::processed
   :post post})
