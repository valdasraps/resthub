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
package lt.emasina.resthub.util;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import lombok.RequiredArgsConstructor;
import lt.emasina.resthub.model.MdTables;

/**
 * Generates schema
 * @author valdo
 */
@RequiredArgsConstructor
public class GenerateSchema extends SchemaOutputResolver {

    private final String fileName;
    
    @Override
    public Result createOutput(String namespaceUri, String suggestedFileName ) throws IOException {
        File file = new File(fileName);
        file.getParentFile().mkdirs();
        StreamResult result = new StreamResult(file);
        result.setSystemId(file.toURI().toURL().toString());
        return result;
    }
    
    public static final void main(String[] args) throws JAXBException, IOException {
        JAXBContext context = JAXBContext.newInstance(MdTables.class);
        context.generateSchema(new GenerateSchema(args[0]));
    }
    
}
