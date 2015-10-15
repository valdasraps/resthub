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
