package lt.emasina.resthub.factory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.TableFactory;
import lt.emasina.resthub.model.MdTable;
import lt.emasina.resthub.model.MdTables;
import org.xml.sax.SAXException;

@Log4j
public abstract class XmlTableFactory implements TableFactory {

    private static final String XML_SCHEMA_FILE = "/lt/emasina/resthub/factory/xml/schema.xsd";
    private static final SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
    
    protected final String tablesFile;
    private final JAXBContext context;
    private final Unmarshaller unmarshaller;
    
    public XmlTableFactory(String tablesFile) throws IOException, SAXException, JAXBException {
        this.tablesFile = tablesFile;
        this.context = JAXBContext.newInstance(MdTables.class);
        this.unmarshaller = context.createUnmarshaller();
        try (InputStream is = XmlTableFactory.class.getResourceAsStream(XML_SCHEMA_FILE)) {
            this.unmarshaller.setSchema(factory.newSchema(new StreamSource(is)));
        }
    }
    
    @Override
    public boolean isRefresh(Date lastUpdate) {
        return lastUpdate == null;
    }

    protected List<MdTable> getTables(InputStream is) throws Exception {
        MdTables mdTables = (MdTables) unmarshaller.unmarshal(is);
        return mdTables.getTables();
    }

    @Override
    public boolean isRefreshable() {
        return false;
    }

}
