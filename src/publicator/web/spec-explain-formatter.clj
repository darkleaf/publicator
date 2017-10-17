(ns publicator.web.spec-explain-formatter
  (:require
   [clojure.spec.alpha :as s]
   [akar.syntax :refer [match]]
   [akar.patterns :refer :all]

   [publicator.interactors.user.register :as interactor]))

(defn- !required-key [problem]
  (comment
    {:path [], :val {}, :in []
     :pred (clojure.core/fn [%] (clojure.core/contains? % :SOME/K))
     :via [:SOME/SPEC-1 :SOME/SPEC-2]})
  (match problem
         {:via (:view (comp first s/form peek) [(!constant `s/keys)])
          :pred (:view (comp last last) k)
          :in in}
         [(conj in k)]
         :_ nil))

(defn- !min-max-regex [char-pattern]
  (fn [problem]
    (comment
      {:path [:password], :val "1234", :in [:password]
       :pred (clojure.core/fn [%]
               (clojure.core/re-matches #".{8,255}" %)),
       :via []})
    (match problem
           {:pred (:and
                   (:view (comp first last)
                          [(!constant `re-matches)])
                   (:view (comp str second last)
                          [(!regex (re-pattern
                                    (str "\\A"
                                         "\\" char-pattern
                                         "\\{(\\d+),(\\d+)\\}\\z")))
                           (:view bigint r-min)
                           (:view bigint r-max)]))
            :in in}
           [in r-min r-max]
           :_ nil)))

(format (s/explain-data ::interactor/params {:full-name ""
                                             :login ""}))

(defn- format-problem [problem]
  (match problem
         [!required-key in]
         [in "Обязательное"]

         [(!min-max-regex ".") in r-min r-max]
         [in (str "Кол-во символов от " r-min " до " r-max)]

         [(!min-max-regex "\\w") in r-min r-max]
         [in (str "Кол-во латинских букв и цифр от " r-min " до " r-max "]")]))


(defn format [explain-data]
  (let [problems (::s/problems explain-data)]
    (map format-problem problems)))
