package lt.emasina.resthub.factory;

import java.io.InputStream;
import java.util.List;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.model.MdTable;

@Log4j
public class XmlResourceTableFactory extends XmlTableFactory {

    public XmlResourceTableFactory(String tablesResource) {
        super(tablesResource);
    }

    @Override
    public List<MdTable> getTables() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(tablesFile)) {
            return getTables(is);
        } catch (Exception ex) {
            log.fatal("Error while loading XML file", ex);
        }
        return null;
    }

    @Override
    public void close() throws Exception { }
    
}
