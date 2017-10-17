(ns publicator.web.user.register.errors-presenter
  (:require
   [clojure.spec.alpha :as s]

   [publicator.interactors.user.register :as interactor]
   [publicator.domain.user :as user]

   [clojure.core.match :as m]
   [akar.syntax :as ak]
   [akar.patterns :as ak.p]))

(def no-errors {})

(def already-registered {:form-ujs/errors ["Уже зарегистрирован"]})

(defn- contains?-match [pred]
    (m/match pred
             ([fn ['%] ([contains? '% k] :seq)] :seq) [:ok k]
             :else :no-match))


(defn- handle-problem [problem]
  (akar/match problem
              {:via (:view last ::interactor/params), :in in}
              :ok))


(let [data     (s/explain-data ::interactor/params {})
      problems (::s/problems data)
      problem  (first problems)]
  (ak/match problem
            {:via (:view (comp first s/form peek) [(ak.p/!constant `s/keys)])}
            :ok))


(akar/match {:key `x}
            {:key ~x}
            x)


(comment
  {:via [::interactor/params ::user/login], :in in}
  [in "Минимум 3 символа"]

  {:via [::interactor/params ::user/full-name], :in in}
  [in "Минимум 2 символа"]

  {:via [::interactor/params ::user/password], :in in}
  [in "Минимум 8 символов"])






(defn- add-error [errors problem]
  (let [[path message] (handle-problem problem)
        path           (conj path :form-ujs/errors)]
    (update-in errors path #(conj (vec %) message))))

(defn explain-data [explain-data]
  (reduce add-error {} (::s/problems explain-data)))
