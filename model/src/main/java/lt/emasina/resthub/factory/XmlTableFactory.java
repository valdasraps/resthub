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
import java.util.regex.Pattern;
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
public abstract class XmlTableFactory extends TableFactory {

    protected static final Pattern XML_FILE = Pattern.compile(".*\\.xml", Pattern.CASE_INSENSITIVE);
    private static final String XML_SCHEMA_FILE = "/lt/emasina/resthub/factory/xml/schema.xsd";
    private static final SchemaFactory FACTORY = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
    
    private final JAXBContext context;
    private final Unmarshaller unmarshaller;
    private boolean loadedOnce = false;
    
    public XmlTableFactory() throws IOException, SAXException, JAXBException {
        this.context = JAXBContext.newInstance(MdTables.class);
        this.unmarshaller = context.createUnmarshaller();
        try (InputStream is = XmlTableFactory.class.getResourceAsStream(XML_SCHEMA_FILE)) {
            this.unmarshaller.setSchema(FACTORY.newSchema(new StreamSource(is)));
        }
    }
    
    protected List<MdTable> getTables(InputStream is) throws Exception {
        MdTables mdTables = (MdTables) unmarshaller.unmarshal(is);
        this.loadedOnce = true;
        return mdTables.getTables();
    }

    @Override
    public boolean isRefresh() {
        return ! loadedOnce;
    }

}
