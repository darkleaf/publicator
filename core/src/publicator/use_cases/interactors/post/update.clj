(ns publicator.use-cases.interactors.post.update
  (:require
   [publicator.domain.aggregates.post :as post]
   [publicator.domain.identity :as identity]
   [publicator.use-cases.services.user-session :as user-session]
   [publicator.use-cases.abstractions.storage :as storage]
   [publicator.utils.spec :as utils.spec]
   [darkleaf.either :as e]
   [clojure.spec.alpha :as s]))

(s/def ::params (utils.spec/only-keys :req-un [::post/title ::post/content]))

(defn- check-authorization= [t id]
  (e/let= [iuser (user-session/iuser t)
           ok    (if (nil? iuser)
                   (e/left [::logged-out]))
           ipost (storage/get-one t id)
           ok    (if (nil? ipost)
                   (e/left [::not-found]))
           ok    (if-not (contains? (:posts-ids @iuser) id)
                   (e/left [::not-authorized]))]
    [::authorized]))

(defn- check-params= [params]
  (if-some [ed (s/explain-data ::params params)]
    (e/left [::invalid-params ed])))

(defn- update-post [ipost params]
  (dosync (alter ipost merge params)))

(defn- post->params [post]
  (select-keys post [:title :content]))

(defn initial-params [id]
  (storage/with-tx t
    @(e/let= [ok     (check-authorization= t id)
              ipost  (storage/get-one t id)
              params (post->params @ipost)]
       [::initial-params @ipost params])))

(defn process [id params]
  (storage/with-tx t
    @(e/let= [ok    (check-authorization= t id)
              ok    (check-params= params)
              ipost (storage/get-one t id)]
       (update-post ipost params)
       [::processed @ipost])))

(defn authorize [ids]
  (storage/with-tx t
    (storage/preload t ids)
    (->> ids
         (map #(check-authorization= t %))
         (map deref))))

(s/def ::logged-out (s/tuple #{::logged-out}))
(s/def ::invalid-params (s/tuple #{::invalid-params} map?))
(s/def ::not-found (s/tuple #{::not-found}))
(s/def ::not-authorized (s/tuple #{::not-authorized}))
(s/def ::initial-params (s/tuple #{::initial-params} ::post/post map?))
(s/def ::processed (s/tuple #{::processed} ::post/post))
(s/def ::authorized (s/tuple #{::authorized}))

(s/fdef initial-params
  :args (s/cat :id ::post/id)
  :ret (s/or :ok  ::initial-params
             :err ::logged-out
             :err ::not-authorized
             :err ::not-found))

(s/fdef process
  :args (s/cat :id ::post/id
               :params any?)
  :ret (s/or :ok  ::processed
             :err ::logged-out
             :err ::not-authorized
             :err ::not-found
             :err ::invalid-params))

(s/fdef authorize
  :args (s/cat :ids (s/coll-of ::post/id))
  :ret (s/coll-of (s/or :ok  ::authorized
                        :err ::logged-out
                        :err ::not-found
                        :err ::not-authorized)))
