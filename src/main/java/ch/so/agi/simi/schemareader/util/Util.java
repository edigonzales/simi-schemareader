package ch.so.agi.simi.schemareader.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
    
    public static void loadFile(String resourcePath, File copyFile) throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(resourcePath);

        InputStream resourceInputStream;
        resourceInputStream = resource.getInputStream();
        Files.copy(resourceInputStream, copyFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
