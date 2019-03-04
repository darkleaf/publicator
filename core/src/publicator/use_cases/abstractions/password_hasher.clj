(ns publicator.use-cases.abstractions.password-hasher)

;; check нужет, т.к. derive для одного и того же пароля может давать разные результаты,
;; т.к. результат может содержать случайную соль

(declare ^{:dynamic true, :arglists '([password])}
         *derive*

         ^{:dynamic true, :arglists '([attempt encrypted])}
         *check*)
