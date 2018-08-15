(ns publicator.web.presenters.post.list
  (:require
   [publicator.use-cases.interactors.post.list :as interactor]
   [publicator.use-cases.interactors.post.create :as create-interactor]
   [publicator.domain.aggregates.user :as user]
   [publicator.web.routing :as routing]))

(defn- post->model [post]
  {:id             (:id post)
   :url            (routing/path-for :post.show/process {:id (-> post :id str)})
   :edit-url       (routing/path-for :post.update/initial-params  {:id (-> post :id str)})
   :title          (:title post)
   :can-edit?      (::interactor/can-edit? post)
   :user-full-name (::user/full-name post)})

(defn processed [posts]
  (let [can-create? (= [::create-interactor/authorized]
                       (create-interactor/authorize))]
    (cond-> {}
      :always     (assoc :posts (map post->model posts))
      can-create? (assoc :new {:text "New"
                               :url  (routing/path-for :post.create/initial-params)}))))
