(ns publicator.use-cases.services.user-session
  (:require
   [publicator.use-cases.abstractions.session :as session]
   [publicator.use-cases.abstractions.storage :as storage]
   [publicator.domain.aggregate :as aggregate]))

(defn user-id []
  (session/*get* ::id))

(defn logged-in? []
  (boolean (user-id)))

(defn logged-out? []
  (not (logged-in?)))

(defn log-in! [user]
  (session/*set* ::id (-> user aggregate/root :root/id)))

(defn log-out! []
  (session/*set* ::id nil))

(defn iuser []
  (if-let [id (user-id)]
    (storage/*get* :user id)))

(defn user []
  (storage/transaction
   (some-> (iuser)
           (deref))))
