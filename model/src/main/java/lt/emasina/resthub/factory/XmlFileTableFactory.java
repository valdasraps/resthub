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
