(ns publicator.web.layout
  (:require
   [hiccup.core :refer [html]]
   [hiccup.page :refer [html5 include-js]]
   [publicator.interactors.services.user-session :as user-session]
   [publicator.ring.helpers :refer [path-for]]
   [publicator.web.helpers :as h]))

(defn render [body]
  (str
   (html5
    [:html
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:content "width=device-width, initial-scale=1, shrink-to-fit=no"
              :name "viewport"}]
      [:link
       {:crossorigin "anonymous",
        :integrity "sha384-/Y6pD6FV/Vv2HJnA6t+vslU6fwYXjCFtcEpHbNJ0lyAFsXTsjBbfaDjzALeQsN6M"
        :href "https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css",
        :rel "stylesheet"}]]
     [:body
      [:nav.navbar.navbar-expand-lg.navbar-light.bg-light
       [:div.container
        (h/link-to "Publicator" (path-for :root)
                   :class "navbar-brand")
        [:div.navbar-nav.mr-auto
         (h/link-to "Posts" (path-for :post.list/handler)
                    :class "nav-item nav-link")]

        [:div.navbar-nav
         (when (user-session/logged-in?)
           (h/action-btn "Log out" (path-for :user.log-out/handler)
                         :class "btn btn-link nav-link"))
         (when (user-session/logged-out?)
           (h/link-to "Register" (path-for :user.register/form)
                      :class "nav-item nav-link"))
         (when (user-session/logged-out?)
           (h/link-to "Log in" (path-for :user.log-in/form)
                      :class "nav-item nav-link"))]]]
      [:div.container body]
      ;; todo: form-ujs helper?
      (include-js "/frontend.js")]])))
