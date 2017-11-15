-- :name- truncate-all :!
DO $$
BEGIN
  EXECUTE (
    SELECT 'TRUNCATE TABLE '
      || string_agg(format('%I', tablename), ', ')
    FROM   pg_tables
    WHERE  schemaname = 'public' AND tablename != 'schema_version'
  );
END $$
