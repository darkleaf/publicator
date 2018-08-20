(ns publicator.web.presenters.post.list
  (:require
   [publicator.use-cases.interactors.post.list :as interactor]
   [publicator.use-cases.interactors.post.create :as interactors.post.create]
   [publicator.use-cases.interactors.post.update :as interactors.post.update]
   [publicator.domain.aggregates.user :as user]
   [publicator.web.routing :as routing]))

(defn- post->model [post authorization]
  {:id             (:id post)
   :url            (routing/path-for :post.show/process {:id (-> post :id str)})
   :update-url     (routing/path-for :post.update/initial-params {:id (-> post :id str)})
   :title          (:title post)
   :can-update?    (= [::interactors.post.update/authorized] authorization)
   :user-full-name (::user/full-name post)})

(defn processed [posts]
  (let [authorizations (interactors.post.update/authorize (map :id posts))
        view-models    (map post->model posts authorizations)
        can-create?    (= [::interactors.post.create/authorized]
                          (interactors.post.create/authorize))]
    (cond-> {}
      :always     (assoc :posts view-models)
      can-create? (assoc :new {:text "New"
                               :url  (routing/path-for :post.create/initial-params)}))))
