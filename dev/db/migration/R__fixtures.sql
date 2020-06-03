truncate table "user";

insert into "user" values
(-1, 'active', true, true, 'admin', 'digest',
 '{2, 3}', '{1, 1}',
 '{2, 3}', '{"en", "ru"}',
 '{2, 3}', '{"John", "Иван"}',
 '{2, 3}', '{"Doe", "Иванов"}');
