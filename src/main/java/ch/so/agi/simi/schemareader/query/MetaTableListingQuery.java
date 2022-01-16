package ch.so.agi.simi.schemareader.query;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import ch.so.agi.simi.schemareader.model.tablelisting.TableListing;
import ch.so.agi.simi.schemareader.model.tablelisting.TableShortInfo;
import ch.so.agi.simi.schemareader.util.Util;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaTableListingQuery {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

	private static final int MAX_RESPONSE_COUNT = 150;
	
    private static final String SCHEMA_QUERY_FILE = "classpath:sql/meta_schema_list.sql";
    private static String SCHEMA_QUERY = null;
    
    private static final String TABLE_QUERY_FILE = "classpath:sql/meta_table_list.sql";
    private static String TABLE_QUERY = null;

	private JdbcTemplate dbClient;
	private String schema;
	private String model;
	
	public static TableListing queryTables(JdbcTemplate dbClient, String schema, String model) {
		
		MetaTableListingQuery queryExec = new MetaTableListingQuery(dbClient, schema, model);
		
		TableListing listing = queryExec.queryCatalogue();
		
		return listing;
	}
	
	private MetaTableListingQuery(JdbcTemplate dbClient, String schema, String model) {
		
		if (dbClient == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Argument dbClient must not be null");
					
		if (StringUtils.isBlank(schema) || StringUtils.isBlank(model))
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Schema and model url params must be given and not blank");
		
		if (SCHEMA_QUERY == null)
		    SCHEMA_QUERY = Util.loadUtf8(SCHEMA_QUERY_FILE);
		
		if (TABLE_QUERY == null)
			TABLE_QUERY = Util.loadUtf8(TABLE_QUERY_FILE);
		
		this.dbClient = dbClient;
		this.model = model;
		this.schema = schema;
	}
	
	private TableListing queryCatalogue() {
			
		List<TableShortInfo> list = null;
		Integer truncatedTo = null;

		String modelSql = model + "%";
		
		// TODO: verhindert das sql injection?
		String schemaName = (String) dbClient.queryForObject(
		            SCHEMA_QUERY, String.class, schema
		        );
		
		String query  = TABLE_QUERY.replace("${schema}", schemaName);
		list = dbClient.query(
		                query, 
						new BeanPropertyRowMapper<TableShortInfo>(TableShortInfo.class),
						modelSql
						);
		
		log.info(String.valueOf(list.size()));
		
		if (list.size() > MAX_RESPONSE_COUNT) {
			list = list.subList(0, MAX_RESPONSE_COUNT);
			truncatedTo = MAX_RESPONSE_COUNT;
		}
		
		TableListing tl = new TableListing();
		tl.setTableViewList(list);
		tl.setTruncatedTo(truncatedTo);
		
		return tl;
	}

	private static String replaceWildcards(String filterVal){
		if(filterVal == null)
			return null;

		return filterVal.trim().replace("*", "%");
	}
}

