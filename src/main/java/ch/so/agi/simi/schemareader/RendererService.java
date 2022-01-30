package ch.so.agi.simi.schemareader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.so.agi.simi.schemareader.util.Util;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;

@Service
public class RendererService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String XSL2HTML_FILE = "xsl/xml2html.xsl"; 
    
    public File getResponseAsHtml(File xmlFile, File tmpFolder) throws IOException, SaxonApiException {
        File xslFile = Paths.get(tmpFolder.getAbsolutePath(), "xml2html.xsl").toFile();
        Util.loadFile(XSL2HTML_FILE, xslFile);
        
        File htmlFile = Paths.get(tmpFolder.getAbsolutePath(), "datenbeschreibung.html").toFile();
        log.info(htmlFile.getAbsolutePath());
        
        Processor processor = new Processor(false);
        XsltCompiler compiler = processor.newXsltCompiler();
        XsltExecutable stylesheet = compiler.compile(new StreamSource(xslFile));
        Serializer out = processor.newSerializer(htmlFile);
        //out.setOutputProperty(Serializer.Property.METHOD, "html");
        //out.setOutputProperty(Serializer.Property.INDENT, "yes");
        Xslt30Transformer transformer = stylesheet.load30();
        transformer.transform(new StreamSource(xmlFile), out);

        return htmlFile;
    }
    
}
