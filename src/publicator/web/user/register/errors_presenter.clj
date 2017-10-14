(ns publicator.web.user.register.errors-presenter
  (:require
   [clojure.spec.alpha :as s]
   [clojure.core.match :as m]
   [publicator.interactors.user.register :as interactor]
   [publicator.domain.user :as user]))

(def no-errors {})

(def already-registered {})

(defn- contains?-match [pred]
    (m/match pred
             ([fn ['%] ([contains? '% k] :seq)] :seq) [:ok k]
             :else :no-match))

(defn- handle-problem [problem]
  (m/match problem
           {:via [::interactor/params], :in in
            :pred ([:ok k] :<< contains?-match)}
           [(conj in k) "Поле должно быть заполнено"]

           {:via [::interactor/params ::user/login], :in in}
           [in "Минимум 3 символа"]

           {:via [::interactor/params ::user/full-name, :in in]}
           [in "Минимум 2 символа"]

           {:via [::interactor/params ::user/password], :in in}
           [in "Минимум 8 символов"]))

(defn- add-error [errors problem]
  (let [[path message] (handle-problem problem)
        path           (conj path :form-ujs/error)]

    ;; может ли быть несколько проблем у одного поля?
    ;; (update-in errors path conj message)
    (assoc-in errors path message)))


(defn explain-data [explain-data]
  (reduce add-error {} (::s/problems explain-data)))
