package net.resthub.factory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.log4j.Log4j;
import net.resthub.model.MdTable;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

/**
 * XmlClasspathFactory
 * @author valdo
 */
@Log4j
public class XmlClasspathFactory extends XmlTableFactory {

    private static final Pattern XML_FILE = Pattern.compile(".*\\.xml");

    private final Reflections reflections;
    
    public XmlClasspathFactory(String tablesClassPath) {
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
                List<MdTable> t = getTables(is);
                int numTables = 0;
                if (t != null) {
                    tables.addAll(t);
                    numTables = t.size();
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
