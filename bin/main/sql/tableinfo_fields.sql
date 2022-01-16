WITH 

att_base AS (
	SELECT 
		nspname AS schema_name,
		relname AS tv_name,
		attrelid AS tv_oid,
		attnum AS att_num,
		attname AS att_name,
		attnotnull AS att_mandatory,
		typname AS att_typ,
		NULLIF(information_schema._pg_char_max_length(atttypid, atttypmod), -1) AS att_length
	FROM	
		pg_namespace
	JOIN 
		pg_class 
			ON  pg_namespace.oid = pg_class.relnamespace
	JOIN 
		pg_attribute
			ON pg_class.oid = pg_attribute.attrelid
	JOIN 
		pg_type 
			ON pg_attribute.atttypid = pg_type.oid
	WHERE
		attnum > 0 --exclude system columns
),

geo_att AS (
	SELECT 
		f_table_schema AS geo_schema_name,
		f_table_name AS geo_table_name,
		f_geometry_column AS geo_att_name,
		"type" AS geo_col_type,
		refsys.auth_srid AS geo_col_sr_id,   
		refsys.auth_name AS geo_col_sr_org
	FROM 
		geometry_columns gc 
	JOIN
		spatial_ref_sys refsys 
			ON gc.srid = refsys.srid 
),

att_desc AS ( 
	SELECT 
		objoid AS tv_oid, 
		objsubid AS att_num, 
		description AS att_desc 
	FROM 
		pg_description 
),

att_all AS (
	SELECT
		schema_name,
		tv_name,
		att_name as name,
		att_mandatory as mandatory,
		att_typ as type,
		att_length as length,
		att_desc as description,
		geo_col_type as geo_field_type,
		geo_col_sr_id as geo_field_sr_id,
		geo_col_sr_org as geo_field_sr_org  
	FROM 
		att_base
	LEFT JOIN 
		att_desc
			ON att_base.tv_oid = att_desc.tv_oid 
			AND att_base.att_num = att_desc.att_num
	LEFT JOIN 
		geo_att
			ON att_base.schema_name = geo_att.geo_schema_name
			AND att_base.tv_name = geo_att.geo_table_name
			AND att_base.att_name = geo_att_name
)

SELECT
	*
FROM 
	att_all
WHERE 
		schema_name = ? 
	AND 
		tv_name = ?
;