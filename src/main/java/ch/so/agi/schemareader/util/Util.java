package ch.so.agi.schemareader.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.server.ResponseStatusException;

public class Util {
    public static String loadUtf8(String resourcePath) {
    	
    	String fileText = null;
    	
    	if(resourcePath == null)
    		return null;
    	
    	ResourceLoader resourceLoader = new DefaultResourceLoader();
    	Resource resource = resourceLoader.getResource(resourcePath);
    	
        try (Reader reader = new InputStreamReader(resource.getInputStream(), "UTF-8")) {
            fileText = FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new ResponseStatusException(
            							HttpStatus.INTERNAL_SERVER_ERROR,
            							"Could not find file " + resource.toString(),
            							e);
        }
        
        return fileText;
    }

}
