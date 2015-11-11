/*
 * #%L
 * model
 * %%
 * Copyright (C) 2012 - 2015 valdasraps
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package lt.emasina.resthub.factory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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

    private final Reflections reflections;
    
    public XmlClasspathFactory(String tablesClassPath) throws IOException, SAXException, JAXBException {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Will be looking for tables xml at %s", tablesClassPath));
        }
        this.reflections = new Reflections(tablesClassPath, new ResourcesScanner());
    }
    
    @Override
    public List<MdTable> getTables() {
        List<MdTable> tables = new ArrayList<>();

        for (String xmlFile: reflections.getResources(XML_FILE)) {
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
