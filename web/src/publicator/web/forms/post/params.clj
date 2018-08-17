(ns publicator.web.forms.post.params
  (:require
   [publicator.web.routing :as routing]))

(defn description [{:keys [url method]}]
  {:widget :submit, :name "Готово"
   :url url, :method method, :nested
   {:widget :group, :nested
    [:title {:widget :input, :label "Заголовок"}
     :content {:widget :textarea, :label "Содержание"}]}})

(defn build-create [initial-params]
  (let [cfg {:url    (routing/path-for :post.create/process)
             :method :post}]
    {:initial-data initial-params
     :errors       {}
     :description  (description cfg)}))

(defn build-update [id initial-params]
  (let [cfg {:url    (routing/path-for :post.update/process {:id (str id)})
             :method :post}]
    {:initial-data initial-params
     :errors       {}
     :description  (description cfg)}))
