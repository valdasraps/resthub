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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.List;
import javax.xml.bind.JAXBException;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.model.MdTable;
import org.xml.sax.SAXException;

@Log4j
public class XmlFileTableFactory extends XmlTableFactory {
    
    private final Path file;
    
    private FileTime lastModified = null;
    private Boolean fileFound = null;
    
    public XmlFileTableFactory(String tablesFile) throws IOException, SAXException, JAXBException {
        this.file = Paths.get(tablesFile);
    }
    
    @Override
    public List<MdTable> getTables() {
        try (FileInputStream is = new FileInputStream(this.file.toFile())) {
            
            this.lastModified = Files.getLastModifiedTime(file);
            this.fileFound = true;
            return getTables(is);
            
        } catch (Exception ex) {
            
            this.fileFound = ! ex.getClass().equals(FileNotFoundException.class);
            log.fatal("Error while loading XML file", ex);
	
        }
        return null;
    }

    @Override
    public void close() throws Exception { }

    @Override
    public boolean isRefresh() {
        if (this.lastModified == null) {
            
            return Boolean.TRUE;
            
        } else {
            
            try {
                
                return Files.getLastModifiedTime(file).compareTo(this.lastModified) != 0 || fileFound == false;
                
            } catch (IOException ex) {
                
                return fileFound;
                
            }
            
        }
    }
    
}
