(ns publicator.persistence.test-system
  (:require
   [clojure.test :as t]
   [com.stuartsierra.component :as component]
   [darkleaf.effect.core :as e]
   [darkleaf.effect.middleware.contract :as contract]
   [publicator.config :refer [config]]
   [publicator.core.use-cases.contracts :as contracts]
   [publicator.persistence.components :as components]
   [publicator.persistence.handlers :as handlers]))

(defn test-system []
  (let [cfg (config :test)]
    (component/system-map
     :sourceable (:jdbc cfg)
     :transactable (component/using (components/test-transactable) {:connectable :sourceable})
     :migration (component/using (components/migration) [:sourceable]))))

(defmacro with-system [[sys-name sys-form] & body]
  {:pre [(some? sys-name)
         (some? sys-form)]}
  `(let [~sys-name (component/start-system ~sys-form)]
     (try
       ~@body
       (finally
         (component/stop-system ~sys-name)))))

(defn wrap [f*]
  (-> f*
      (e/wrap)
      #_(contract/wrap-contract)))

(defn run [f* & args]
  (with-system [system (test-system)]
    (let [f*                     (wrap f*)
          {:keys [transactable]} system]
      (handlers/with-handlers [handlers transactable]
        (e/perform handlers (apply f* args))))


    #_(let [contracts    (merge @contracts/registry
                                {'my/f* {:args   (constantly true)
                                         :return (constantly true)}})
            continuation (-> f*
                             (contract/wrap-contract contracts 'my/f*)
                             #_(context/wrap-context))])))
      ;;     handlers     (sut/handlers)
      ;;     perform      (-> e/perform
      ;;                      (sut/wrap-tx (:transactable system)))
      ;;     ctx          {}
      ;;     [ctx result] (perform handlers continuation [ctx args])]
      ;; result)))
