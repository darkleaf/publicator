(ns publicator.views.registration.new
  (:require
   [hiccup.core :refer [html]]
   [hiccup.page :refer [html5 include-js]]
   [form-ujs.core :as form]
   [publicator.interactors.user.register :as register]
   [cognitect.transit :as t])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]))

(defn serialize [d]
  (let [out (ByteArrayOutputStream. 4096)
        writer (t/writer out :json)
        _ (t/write writer d)]
    (.toString out)))

(defn layout [ctx page]
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
       [:div..container
        [:a.navbar-brand {:href "#"} "Publicator"]

        [:div.navbar-nav.mr-auto
         [:a.nav-item.nav-link {:href "#"} "Posts"]
         [:a.nav-item.nav-link {:href "#"} "Users"]]

        [:div.navbar-nav
         [:a.nav-item.nav-link {:href "#"} "Register"]
         [:a.nav-item.nav-link {:href "#"} "Log in"]]]]
      [:div.container page]
      (include-js "http://localhost:4200/main.js")]])))


(defn form [description data errors]
  (html
   [:div
    [:div {:data-form-ujs :register}]
    [:script
     {:id "register-description"
      :type "application/transit+json"}
     (serialize description)]
    [:script
     {:id "register-data"
      :type "application/transit+json"}
     (serialize data)]]))

(defn description []
  (let [desc (form/spec->widget ::register/params)]
    {:id :register
     :widget :submit
     :url "/registration"
     :method :post
     :body desc}))

(defn page [ctx]
  (form (description)
        {} ;; initial params
        nil))

(defn render [ctx]
  (layout ctx (page ctx)))
