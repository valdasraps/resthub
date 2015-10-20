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
import java.util.List;
import javax.xml.bind.JAXBException;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.model.MdTable;
import org.xml.sax.SAXException;

@Log4j
public class XmlResourceTableFactory extends XmlTableFactory {

    public XmlResourceTableFactory(String tablesResource) throws IOException, SAXException, JAXBException {
        super(tablesResource);
    }

    @Override
    public List<MdTable> getTables() throws Exception {
        try (InputStream is = XmlResourceTableFactory.class.getResourceAsStream(tablesFile)) {
            return getTables(is);
        }
    }

    @Override
    public void close() throws Exception { }
    
}
