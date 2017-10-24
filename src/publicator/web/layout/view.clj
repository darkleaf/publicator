(ns publicator.web.layout.view
  (:require
   [hiccup.core :refer [html]]
   [hiccup.page :refer [html5 include-js]]
   [io.pedestal.http.route :as route]
   [publicator.interactors.services.user-session :as user-session]))

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
        [:a.navbar-brand {:href "#"} "Publicator"]

        [:div.navbar-nav.mr-auto
         [:a.nav-item.nav-link {:href "#"} "Posts"]
         [:a.nav-item.nav-link {:href "#"} "Users"]]

        [:div.navbar-nav
         (when (user-session/logged-in?)
           [:form {:action (route/url-for :user-log-out)
                   :method :post}
            [:button.btn.btn-link.nav-link {:type :submit} "Log out"]])
         (when (user-session/logged-out?)
           [:a.nav-item.nav-link
            {:href (route/url-for :user-register-form)}
            "Register"])
         (when (user-session/logged-out?)
           [:a.nav-item.nav-link
            {:href (route/url-for :user-log-in-form)}
            "Log in"])]]]

      [:div.container body]
      (include-js "http://localhost:4200/main.js")]])))
