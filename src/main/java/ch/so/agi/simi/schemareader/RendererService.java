package ch.so.agi.simi.schemareader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;

import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import ch.so.agi.simi.schemareader.util.Util;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SAXDestination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

@Service
public class RendererService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String XSL2HTML_FILE = "xsl/xml2html.xsl"; 
    private static final String XSL2PDF_FILE = "xsl/xml2pdf.xsl"; 
    
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
    
    public File getResponseAsPdf(File xmlFile, File tmpFolder) throws IOException, SaxonApiException, SAXException {
        File xslFile = Paths.get(tmpFolder.getAbsolutePath(), "xml2pdf.xsl").toFile();
        Util.loadFile(XSL2PDF_FILE, xslFile);
        
        File confFile = Paths.get(tmpFolder.getAbsolutePath(), "fop.xconf").toFile();
        File ttfRegularFile = Paths.get(tmpFolder.getAbsolutePath(), "FrutigerLTCom-Roman.ttf").toFile();
        File ttfItalicFile = Paths.get(tmpFolder.getAbsolutePath(), "FrutigerLTCom-Italic.ttf").toFile();
        File ttfBlackFile = Paths.get(tmpFolder.getAbsolutePath(), "FrutigerLTCom-Black.ttf").toFile();
        File ttfBlackItalicFile = Paths.get(tmpFolder.getAbsolutePath(), "FrutigerLTCom-BlackItalic.ttf").toFile();
        Util.loadFile("xsl/fop.xconf", confFile);
        Util.loadFile("xsl/FrutigerLTCom-Roman.ttf", ttfRegularFile);
        Util.loadFile("xsl/FrutigerLTCom-Italic.ttf", ttfItalicFile);
        Util.loadFile("xsl/FrutigerLTCom-Black.ttf", ttfBlackFile);
        Util.loadFile("xsl/FrutigerLTCom-BlackItalic.ttf", ttfBlackItalicFile);
   
        File pdfFile = Paths.get(tmpFolder.getAbsolutePath(), "datenbeschreibung.pdf").toFile();
        log.info(pdfFile.getAbsolutePath());

        Processor processor = new Processor(false);
        XsltCompiler compiler = processor.newXsltCompiler();
        XsltExecutable exp = compiler.compile(new StreamSource(xslFile));
        XdmNode source = processor.newDocumentBuilder().build(new StreamSource(xmlFile));
        XsltTransformer trans = exp.load();
        trans.setInitialContextNode(source);
    
        FopFactory fopFactory = FopFactory.newInstance(Paths.get(tmpFolder.getAbsolutePath(), "fop.xconf").toFile());
        OutputStream outPdf = new BufferedOutputStream(new FileOutputStream(pdfFile)); 
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, outPdf);

        trans.setDestination(new SAXDestination(fop.getDefaultHandler()));
        trans.transform();
        outPdf.close();
        trans.close();
        
        return pdfFile;
    }
    
}
