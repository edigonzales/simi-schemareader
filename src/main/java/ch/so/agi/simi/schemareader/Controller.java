package ch.so.agi.simi.schemareader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import ch.so.agi.simi.schemareader.dbclients.DbClientMap;
import ch.so.agi.simi.schemareader.model.tableinfo.TableAndFieldInfo;
import ch.so.agi.simi.schemareader.model.tablelisting.TableListing;
import ch.so.agi.simi.schemareader.model.tablelisting.TableShortInfo;
import ch.so.agi.simi.schemareader.query.MetaTableInfoQuery;
import ch.so.agi.simi.schemareader.query.MetaTableListingQuery;
import ch.so.agi.simi.schemareader.query.TableInfoQuery;
import ch.so.agi.simi.schemareader.query.TableListingQuery;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class Controller {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	DbClientMap dbClients;
	
	@Autowired
	RendererService renderer;
	
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
    
    @RequestMapping(value = "/meta/{db}/{schema}/{model}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_HTML_VALUE})
    public ResponseEntity<?> listMetaTables(
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
        
        List<TableAndFieldInfo> tciList = new ArrayList<>();
        for (int i=0; i<res.getTableViewList().size(); i++) {
            TableShortInfo tsi = res.getTableViewList().get(i);
            log.info(tsi.getTvName());
            TableAndFieldInfo tci = MetaTableInfoQuery.queryTableInfo(dbClient, schema, tsi.getTvName(), model);
            tciList.add(tci);
        }
              
        try {
            if (!outputFormat.equalsIgnoreCase("xml")) {
                File tmpFolder = Files.createTempDirectory("metaws-").toFile();
                File xmlFile = Paths.get(tmpFolder.getAbsolutePath(), "datenbeschreibung.xml").toFile();
                XmlMapper xmlMapper = new XmlMapper();
                String xmlString = xmlMapper.writeValueAsString(tciList);
                Files.write(Paths.get(xmlFile.getAbsolutePath()), "<?xml version=\"1.0\"?>".getBytes(), StandardOpenOption.CREATE);
                Files.write(Paths.get(xmlFile.getAbsolutePath()), xmlString.getBytes(), StandardOpenOption.APPEND);
                log.info(xmlFile.getAbsolutePath());
                
                if (outputFormat.equalsIgnoreCase("html")) {
                    // TODO Die Datei ist vorhanden, kann man statischen Content irgendwie elegant ausliefern?
                    File htmlFile = renderer.getResponseAsHtml(xmlFile, tmpFolder);
                    String content = new String(Files.readAllBytes(Paths.get(htmlFile.getAbsolutePath())));
                    return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(content);
                }
            }
            
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(tciList);            
        } catch (Exception e) { // FIXME
            throw new IllegalStateException(e);
        }
        
    } 
    
}

