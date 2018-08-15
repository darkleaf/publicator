(ns publicator.web.forms.user.register
  (:require
   [publicator.web.routing :as routing]))

(defn description []
  {:widget :submit, :name "Зарегистрироваться"
   :url (routing/path-for :user.register/process), :method :post, :nested
   {:widget :group, :nested
    [:login {:widget :input, :label "Логин"}
     :full-name {:widget :input, :label "Полное имя"}
     :password {:widget :input, :label "Пароль", :type "password"}]}})

(defn build [initial-params]
  {:initial-data initial-params
   :errors       {}
   :description  (description)})
