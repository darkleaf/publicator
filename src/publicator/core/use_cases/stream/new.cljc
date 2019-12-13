(ns publicator.core.use-cases.stream.new
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.user :as user]
   [darkleaf.effect.core :refer [eff !]]))

(def allowed-attrs #{:stream.translation/lang
                     :stream.translation/name
                     :stream.translation/stream})

(defn- check-additional-attrs [datoms]
  (eff
    (if-some [additional (->> datoms
                              (map :a)
                              (remove allowed-attrs)
                              (set)
                              (not-empty))]
      (! [:ui/show-additional-attributes-error additional]))))

(defn- fill-defaults [stream]
  (agg/apply-tx stream
                [{:db/ident     :root
                  :stream/state :active}]))

(defn- check-validation-errors [stream]
  (eff
    (if-some [errors (-> stream agg/validate agg/errors not-empty)]
      (! [:ui/show-validation-errors errors]))))

(defn- fill-id [stream]
  (eff
    (let [id (! [:persistence/next-id :stream])]
      (agg/apply-tx stream [[:db/add :root :agg/id id]]))))

(defn precondition []
  (eff
    (let [session (! [:session/get])
          user-id (-> session :current-user-id)
          user    (! [:persistence/find :agg/user user-id])]
      (if-not (and (user/active? user)
                   (user/admin? user))
        [:ui/show-main-screen]))))

(defn process []
  (eff
    (if-some [ex-effect (! (precondition))]
      (! ex-effect)
      (let [stream          (agg/allocate :agg/stream)
            tx-data         (! [:ui/edit stream])
            [stream datoms] (agg/apply-tx* stream tx-data)
            _               (! (check-additional-attrs datoms))
            stream          (! (fill-defaults stream))
            _               (! (check-validation-errors stream))
            stream          (! (fill-id stream))]
        (! [:persistence/save stream])
        (! [:ui/show-main-screen])))))
