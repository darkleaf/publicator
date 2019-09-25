(ns publicator.use-cases.stream.edit
  (:require
   [publicator.util :as u]
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.user :as user]))

(def allowed-attrs #{:stream.translation/lang
                     :stream.translation/name
                     :stream.translation/stream})

(defn- check-additional-attrs [datoms]
  (let [additional (->> datoms
                        (map :a)
                        (remove allowed-attrs)
                        (set))]
    (when (not-empty additional)
      [[:ui/show-additional-attributes-error additional]])))

(defn- find-stream [id next]
  (u/linearize
   [[:persistence/find :agg/stream id] (fn [stream] <>)]
   (if (nil? stream)
     [[:ui/show-main-screen]])
   (next stream)))

(defn- check-validation-errors [stream]
  (let [errors (-> stream agg/validate agg/errors)]
    (when (not-empty errors)
      [[:ui/show-validation-errors errors]])))

(defn precondition [_id callback]
  (u/linearize
   [[:session/get] (fn [session] <>)]
   (let [user-id (-> session :current-user-id)])
   [[:persistence/find :agg/user user-id] (fn [user] <>)]
   (if-not (and (user/active? user)
                (user/admin? user))
     (callback [[:ui/show-main-screen]]))
   (callback nil)))

(defn process [id tx-data]
  (u/linearize
   (precondition id (fn [ex-effect] <>))
   (or ex-effect)
   (find-stream id (fn [stream] <>))
   (let [[stream datoms] (-> stream (agg/apply-tx* tx-data))])
   (or (check-additional-attrs datoms))
   (or (check-validation-errors stream))
   [[:do
     [:persistence/save stream]
     [:ui/show-main-screen]]]))
