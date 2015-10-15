package lt.emasina.resthub.factory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBException;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.model.MdTable;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.xml.sax.SAXException;

/**
 * XmlClasspathFactory
 * @author valdo
 */
@Log4j
public class XmlClasspathFactory extends XmlTableFactory {

    private static final String TABLE_SOURCE_KEY = "Source";
    private static final Pattern XML_FILE = Pattern.compile(".*\\.xml");

    private final Reflections reflections;
    
    public XmlClasspathFactory(String tablesClassPath) throws IOException, SAXException, JAXBException {
        super(tablesClassPath);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Will be looking for tables xml at %s", tablesClassPath));
        }
        this.reflections = new Reflections(tablesClassPath, new ResourcesScanner());
    }
    
    @Override
    public List<MdTable> getTables() {
        List<MdTable> tables = new ArrayList<>();
        
        for (String xmlFile: reflections.getStore().getResources(XML_FILE)) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(xmlFile)) {
                List<MdTable> ts = getTables(is);
                int numTables = 0;
                if (ts != null) {
                    
                    tables.addAll(ts);
                    numTables = ts.size();
                    
                    for (MdTable t: ts) {
                        t.getMetadata().put(TABLE_SOURCE_KEY, xmlFile);
                    }
                    
                }
                log.info(String.format("%d tables loaded from XML file: %s", numTables, xmlFile));
            } catch (Exception ex) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Error while loading tables from XML file %s: %s", xmlFile, ex));
                }
            }
        }
        return tables;
    }

    @Override
    public void close() throws Exception { }
    
}
