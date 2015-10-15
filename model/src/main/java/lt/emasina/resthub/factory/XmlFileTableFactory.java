package lt.emasina.resthub.factory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import javax.xml.bind.JAXBException;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.model.MdTable;
import org.xml.sax.SAXException;

@Log4j
public class XmlFileTableFactory extends XmlTableFactory {
    
    public XmlFileTableFactory(String tablesFile) throws IOException, SAXException, JAXBException {
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
