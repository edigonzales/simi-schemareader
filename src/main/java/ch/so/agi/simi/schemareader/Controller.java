package ch.so.agi.simi.schemareader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.so.agi.simi.schemareader.dbclients.DbClientMap;
import ch.so.agi.simi.schemareader.model.tableinfo.TableAndFieldInfo;
import ch.so.agi.simi.schemareader.model.tablelisting.TableListing;
import ch.so.agi.simi.schemareader.query.MetaTableListingQuery;
import ch.so.agi.simi.schemareader.query.TableInfoQuery;
import ch.so.agi.simi.schemareader.query.TableListingQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController()
public class Controller {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	DbClientMap dbClients;
	
    @RequestMapping("/{db}/{schema}/{table}")
    public TableAndFieldInfo queryTableInfo(
    		@PathVariable(required = true) String db,
    		@PathVariable(required = true) String schema,
    		@PathVariable(required = true) String table){
    	
    	JdbcTemplate dbClient = dbClients.getClient(db);
    	
    	TableAndFieldInfo tci = TableInfoQuery.queryTableInfo(dbClient, schema, table);
    	
    	return tci;
    }
    
    @RequestMapping("/{db}")
    public TableListing listMatchingTables(
    			@PathVariable String db,
	    		@RequestParam(name = "schema", required = false) String schemaNameFragment,
	    		@RequestParam(name = "table", required = false) String tableNameFragment
    		){  
    	  	
  	   	JdbcTemplate dbClient = dbClients.getClient(db);
    	TableListing res = TableListingQuery.queryTables(dbClient, schemaNameFragment, tableNameFragment);
    	
    	return res;
    } 
    
    @RequestMapping("/meta/{db}/{schema}/{model}")
    public TableListing listMetaTables(
            @PathVariable String db,
            @PathVariable String schema,
            @PathVariable String model,
            @RequestParam(name = "format", required = false, defaultValue = "xml") String outputFormat
            ) {
        
        log.info(db);
        log.info(schema);
        log.info(model);
        
        JdbcTemplate dbClient = dbClients.getClient(db);

        TableListing res = MetaTableListingQuery.queryTables(dbClient, schema, model);
        log.info(String.valueOf(res.getTableViewList().size()));
        
        // MetaTableInfoQuery: Parameter ist TableListing, dbClient, model?
        
        
//        return "fubar";
        return res;
        
    } 
    
}

