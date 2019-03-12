(ns publicator.domain.abstractions.occupation)

(declare ^{:dynamic true, :arglists '([attrs-set values-map])
           :doc "Проверяет доступность значений аттрибутов.
  Возвращает `boolean`. `attrs-set` нужно указывать,
  т.к. `values-map` может не содержать всех ключей.
  (*occupied* #{:schedule/teacher_id :schedule/class_id} {:schedule/teacher_id 1})"}
         *occupied*)
