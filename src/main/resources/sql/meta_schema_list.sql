SELECT 
    nspname as schema_name
FROM 
    pg_namespace
WHERE 
    nspname = ?
