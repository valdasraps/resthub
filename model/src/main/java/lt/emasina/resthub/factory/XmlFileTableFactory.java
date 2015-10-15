package lt.emasina.resthub.factory;

import java.io.FileInputStream;
import java.util.List;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.model.MdTable;

@Log4j
public class XmlFileTableFactory extends XmlTableFactory {
    
    public XmlFileTableFactory(String tablesFile) {
        super(tablesFile);
    }
    
    @Override
    public List<MdTable> getTables() {
        try (FileInputStream is = new FileInputStream(tablesFile)) {
            return getTables(is);
        } catch (Exception ex) {
            log.fatal("Error while loading XML file", ex);
	}
        return null;
    }

    @Override
    public void close() throws Exception { }

}
