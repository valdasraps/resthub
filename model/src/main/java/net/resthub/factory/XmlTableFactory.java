package net.resthub.factory;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import net.resthub.TableFactory;
import net.resthub.model.MdTable;
import net.resthub.model.MdTables;

@Log4j
@RequiredArgsConstructor
public abstract class XmlTableFactory implements TableFactory {
    
    protected final String tablesFile;
    
    @Override
    public boolean isRefresh(Date lastUpdate) {
        return lastUpdate == null;
    }

    protected List<MdTable> getTables(InputStream is) throws Exception {
        JAXBContext jaxb = JAXBContext.newInstance(MdTables.class);
        Unmarshaller unm = jaxb.createUnmarshaller();
        MdTables mdTables = (MdTables) unm.unmarshal(is);
        return mdTables.getTables();
    }

    @Override
    public boolean isRefreshable() {
        return false;
    }

}
