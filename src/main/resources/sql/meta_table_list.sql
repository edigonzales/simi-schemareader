/* Reine Datentabellen (ohne enum)
 * classname funktioniert nicht, weil sqlname != Tabellennamen (sondern t_type)
 */
SELECT
    DISTINCT ON (colowner)
    '${schema}' AS schema_name,
    colowner AS tv_name
FROM 
    ${schema}.t_ili2db_attrname 
WHERE 
    iliname LIKE ?