(ns form-ujs.spec.widget
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test :as t]))

(defmulti symbol->widget identity)

(defn set->widget [form] nil)

(defmulti seq->widget first)

(declare spec->widget)

(defn form->widget [form]
  (cond
    (keyword? form) (spec->widget form)
    (symbol? form) (symbol->widget form)
    (set? form) (set->widget form)
    (seq? form) (seq->widget form)))

(defn spec->widget [spec-name]
  (let [form   (s/form spec-name)
        widget (form->widget form)]
    (assoc widget :id spec-name)))

(defmethod symbol->widget `string? [_]
  {:widget :input})

(defmethod seq->widget `s/and [form]
  (let [[_ first-form] form]
    (form->widget first-form)))

(defn- unqualify [kw]
  (-> kw name keyword))

(defmethod seq->widget `s/keys [form]
  (let [[_ & {:keys [req req-un opt opt-un]}] form

        specs   (concat req req-un opt opt-un)
        widgets (zipmap specs (map spec->widget specs))

        keys-map (merge
                  (zipmap req req)
                  (zipmap req-un (map unqualify req-un))
                  ;; todo: видимо, опциональные нужно иначе обрабатывать
                  #_(zipmap opt opt)
                  #_(zipmap opt-un (map unqualify opt-un)))]

    {:widget      :group
     :keys-map    keys-map
     :items-order (vec (concat req req-un opt opt-un))
     :items       widgets}))

(comment
  (s/def ::name string?)
  (s/def ::email (s/and  string?))
  ;;(s/def ::kind #{:user :admin})
  (s/def ::user (s/keys :req [::name ::email]))

  (spec->widget ::user))
