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

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBException;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.model.MdTable;
import org.xml.sax.SAXException;

/**
 * XmlClasspathFactory
 * @author valdo
 */
@Log4j
public class XmlFolderTableFactory extends XmlTableFactory {

    private final File folder;
    private final Map<Path, FileTime> times = new HashMap<>();
        
    public XmlFolderTableFactory(String folderName) throws IOException, SAXException, JAXBException {
        this.folder = Paths.get(folderName).toFile();
        
        if (log.isDebugEnabled()) {
            log.debug(String.format("Will be looking for tables xml at %s", folderName));
        }
    }
    
    private boolean isValid() {
        return folder.exists() && folder.isDirectory() && folder.canRead();
    }
    
    private Collection<Path> getFiles() {
        Set<Path> files = new HashSet<>();
        try {
            for (File xmlFile: folder.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return XML_FILE.matcher(name).matches();
                }

            })) {
                files.add(xmlFile.toPath());
            }
        } catch (NullPointerException ex) { }
        return files;
    }
    
    @Override
    public List<MdTable> getTables() {
        
        List<MdTable> tables = new ArrayList<>();
        this.times.clear();

        if (isValid()) {
            for (Path xmlFile: getFiles()) {
                
                try {
                    times.put(xmlFile, Files.getLastModifiedTime(xmlFile));
                } catch (IOException ex) { }
                
                
                try (InputStream is = new FileInputStream(xmlFile.toFile())) {
                    
                    List<MdTable> ts = getTables(is);
                    int numTables = 0;
                    if (ts != null) {

                        tables.addAll(ts);
                        numTables = ts.size();

                        for (MdTable t: ts) {
                            t.getMetadata().put(TABLE_SOURCE_KEY, xmlFile.toString());
                        }

                    }
                    
                    log.info(String.format("%d tables loaded from XML file: %s", numTables, xmlFile));
                    
                } catch (Exception ex) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Error while loading tables from XML file %s: %s", xmlFile, ex));
                    }
                }
                
            }
        }
        
        return tables;
    }

    @Override
    public void close() throws Exception { }
    
    @Override
    public boolean isRefresh() {
        
        Map<Path, FileTime> saved = new HashMap<>();
        saved.putAll(times);
        
        for (Path path: getFiles()) {
            
            if (!times.containsKey(path)) return true;
            FileTime ft = saved.remove(path);
            
            try {
                if (ft.compareTo(Files.getLastModifiedTime(path)) != 0) {
                    return true;
                }
            } catch (IOException ex) {
                return true;
            }
            
        }
        
        return !saved.isEmpty();
        
    }
    
}
