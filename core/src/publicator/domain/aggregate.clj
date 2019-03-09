(ns publicator.domain.aggregate
  (:require
   [publicator.domain.abstractions.instant :as instant]
   [publicator.utils.datascript.validation :as d.validation]
   [datascript.core :as d]))

(defn root [aggregate]
  (d/entity aggregate :root))

(def ^:const root-q '{:find [[?e ...]]
                      :where [[?e :db/ident :root]]})

(def base-spec
  {:schema        {:root/id {:db/unique :db.unique/identity}}
   :on-build-tx   (fn [] [[:db/add 1 :db/ident :root]
                          [:db/add :root :root/created-at (instant/*now*)]])
   :additional-tx (fn [] [[:db/add :root :root/updated-at (instant/*now*)]])
   :read-only     #{:root/id :root/created-at}
   :validator     (d.validation/compose
                   (d.validation/attributes [:root/id         pos-int?]
                                            [:root/created-at inst?]
                                            [:root/updated-at inst?])
                   (d.validation/in-case-of root-q
                                            [:root/id         some?]
                                            [:root/created-at some?]
                                            [:root/updated-at some?]))})

;; (defn- check-errors! [aggregate]
;;   (if-let [errs (-> aggregate meta :aggregate/errors seq)]
;;     (throw (ex-info "Aggregate has errors" {:type   ::has-errors
;;                                             :errors errs}))))

(defn extend-spec [spec other]
  (cond-> spec
    (contains? other :type)          (assoc :type (:type other))
    :always                          (update :schema
                                             merge (:schema other))
    (contains? other :on-build-tx)   (update :on-build-tx
                                             (fn [old] #(concat (old) ((:on-build-tx other)))))
    (contains? other :additional-tx) (update :additional-tx
                                             (fn [old] #(concat (old) ((:additional-tx other)))))
    :always                          (update :read-only
                                             into (:read-only other))
    (contains? other :validator)     (update :validator
                                             validation/compose (:validator other))))


;; (defn becomes [agg new-spec])  ????

;; (defn allocate [spec tx-data]
;;   (let [spec (extend-spec base-spec spec)
;;         agg (-> (d/empty-db (:schema spec))
;;                 (d/db-with tx-data))]))


    ;; (-> (d/empty-db s)
    ;;     (d/db-with [{:db/ident :root
    ;;                  :root/id  id}])
    ;;     (vary-meta assoc
    ;;                :type type))))

(defn build [spec tx-data]
  (let [spec    (extend-spec base-spec spec)
        tx-data (concat ((:on-build-tx spec))
                        tx-data
                        ((:additional-tx spec)))
        agg     (d/empty-db (:schema spec))
        report  (d/with agg tx-data)
        agg     (:db-after report)
        changes (:tx-data report)
        errors  (d.validation/validate agg (:validator spec))]
    (vary-meta agg merge {:type              (:type spec)
                          :aggregate/changes changes
                          :aggregate/errors  errors})))



  ;;       tx-data   (:tx-data report)
  ;;       aggregate (:db-after report)
  ;;       errors    (validation/validate aggregate
  ;;                                      (validation/compose
  ;;                                       base-validator
  ;;                                       (validator (type aggregate))))]
  ;;   (vary-meta aggregate assoc :aggregate/tx-data tx-data)))

;; (defn change [aggregate tx-data]
;;   (let [tx-data   (concat tx-data
;;                           [[:db/add :root :root/updated-at (instant/*now*)]
;;                            [:db.fn/call check-errors!]])
;;         report    (d/with aggregate tx-data)
;;         tx-data   (:tx-data report)
;;         aggregate (:db-after report)]
;;     (vary-meta aggregate assoc :aggregate/tx-data tx-data)))











;; (defn build! [])
;; (defn change! [])





;; (def spec
;;   {:type          :user
;;    ;; :space     :user, надо выпиливать эти спэйсы и переходить на полиморфные связи, больно сложно
;;    :schema        {}
;;    :on-create-tx  (fn [] [[:db/add :root :root/id 1]])
;;    :additional-tx (fn [] [[:db.fn/call]])
;;    :validator     nil
;;    :read-only     #{:root/id}})

;; (allocate spec)
;; (create spec tx-data)
;; (create! spec tx-data)
;; (change agg tx-data)
;; (change! agg tx-data)


;; ;; при этом можно замержить валидатор или коллбэк для формы
;; ;; можно даже наследовать
;; (storage/*get* user/spec 1)


;; spec нужно в metadata цеплять
