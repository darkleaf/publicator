(ns publicator.use-cases.interactors.post.create
  (:require
   [publicator.use-cases.services.user-session :as user-session]
   [publicator.use-cases.abstractions.storage :as storage]
   [publicator.domain.aggregates.post :as post]
   [publicator.domain.identity :as identity]
   [clojure.spec.alpha :as s]
   [publicator.utils.spec :as utils.spec]
   [darkleaf.either :as e]))

(s/def ::params (utils.spec/only-keys :req-un [::post/title ::post/content]))

(defn- check-authorization= []
  (if (user-session/logged-out?)
    (e/left [::logged-out])
    (e/right [::authorized])))

(defn- check-params= [params]
  (if-let [ed (s/explain-data ::params params)]
    (e/left [::invalid-params ed])))

(defn- create-post [t params]
  (storage/create t (post/build params)))

(defn- set-authorship [t ipost]
  (let [iuser (user-session/iuser t)]
    (dosync (alter iuser update :posts-ids conj (:id @ipost)))))

(defn initial-params []
  @(e/let= [ok (check-authorization=)]
     [::initial-params {}]))

(defn process [params]
  (storage/with-tx t
    @(e/let= [ok    (check-authorization=)
              ok    (check-params= params)
              ipost (create-post t params)]
       (set-authorship t ipost)
       [::processed @ipost])))

(defn authorize []
  @(check-authorization=))

(s/def ::logged-out (s/tuple #{::logged-out}))
(s/def ::invalid-params (s/tuple #{::invalid-params} map?))
(s/def ::initial-params (s/tuple #{::initial-params} map?))
(s/def ::processed (s/tuple #{::processed} ::post/post))
(s/def ::authorized (s/tuple #{::authorized}))

(s/fdef authorize
  :args nil?
  :ret (s/or :ok  ::authorized
             :err ::logged-out))

(s/fdef initial-params
  :args nil?
  :ret (s/or :ok  ::initial-params
             :err ::logged-out))

(s/fdef process
  :args (s/cat :params map?)
  :ret (s/or :ok  ::processed
             :err ::logged-out
             :err ::invalid-params))
