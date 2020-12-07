-- идентификаторы отрицательные, чтобы не заморачиваться с sequences

truncate table "user";

insert into "user" values (
  -1, 'active', true, true, 'admin', 'digest',
  2, 'John', 'Doe',
  3, 'Иван', 'Иванов',

  '{4}', '{1}',
  '{4}', '{"star"}',
  '{4}', '{-1}' -- сам себе ачивку виписал, это некорректно
);
