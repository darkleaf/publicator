(ns publicator.use-cases.abstractions.storage
  (:require
   [publicator.utils.coll :as u.c]))

(declare ^{:dynamic true, :arglists '([state])}
         *create*

         ^{:dynamic true, :arglists '([type ids])}
         *preload*

         ^{:dynamic true, :arglists '([type id])}
         *get*)

         ;; можно сделать надежный after-commit,
         ;; сохранять намерение
         ;; ^{:dynamic true, :arglists '([func-sym & args])}
         ;; *after-commit*)



;; запросы объявляются в отдельных неймспейсах.
;; запросы возвращают id и/или аггрегационные данные.
;; id автора и кол-во его книг

;; можно добавить запрос, явно блокирующий агрегат с нужным типом блокировки.
