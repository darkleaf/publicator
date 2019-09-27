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

(defn- find-stream [id]
  (u/linearize
   [[:persistence/find :agg/stream id] (fn [stream] <>)]
   (if (nil? stream)
     [[:ui/show-main-screen]])
   [[:sub/return stream]]))

(defn- check-validation-errors [stream]
  (let [errors (-> stream agg/validate agg/errors)]
    (when (not-empty errors)
      [[:ui/show-validation-errors errors]])))

(defn precondition [_id]
  (u/linearize
   [[:session/get] (fn [session] <>)]
   (let [user-id (-> session :current-user-id)])
   [[:persistence/find :agg/user user-id] (fn [user] <>)]
   (if-not (and (user/active? user)
                (user/admin? user))
     [[:sub/return [[:ui/show-main-screen]]]])
   [[:sub/return]]))

(defn process [[id tx-data]]
  (u/linearize
   (u/sub precondition id (fn [ex-effect] <>))
   (or ex-effect)
   (u/sub find-stream id (fn [stream] <>))
   (let [[stream datoms] (-> stream (agg/apply-tx* tx-data))])
   (or (check-additional-attrs datoms))
   (or (check-validation-errors stream))
   [[:do
     [:persistence/save stream]
     [:ui/show-main-screen]]]))
