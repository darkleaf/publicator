(ns publicator.web.layout.view
  (:require
   [hiccup.core :refer [html]]
   [hiccup.page :refer [html5 include-js]]
   [io.pedestal.http.route :as route]
   [publicator.interactors.services.user-session :as user-session]
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
        (h/link-to "Publicator" (route/url-for :root)
                   :class "navbar-brand")
        [:div.navbar-nav.mr-auto
         (h/link-to "Posts" (route/url-for :post-list)
                    :class "nav-item nav-link")]

        [:div.navbar-nav
         (when (user-session/logged-in?)
           (h/action "Log out" (route/url-for :user-log-out) :post
                     :class "btn btn-link nav-link"))
         (when (user-session/logged-out?)
           (h/link-to "Register" (route/url-for :user-register-form)
                      :class "nav-item nav-link"))
         (when (user-session/logged-out?)
           (h/link-to "Log in" (route/url-for :user-log-in-form)
                      :class "nav-item nav-link"))]]]
      [:div.container body]
      (include-js "http://localhost:4200/main.js")]])))
