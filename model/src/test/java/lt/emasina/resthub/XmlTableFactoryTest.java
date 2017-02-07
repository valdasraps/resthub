package lt.emasina.resthub;

import java.io.IOException;
import java.util.List;
import javax.xml.bind.JAXBException;
import lt.emasina.resthub.TableFactory;
import lt.emasina.resthub.factory.XmlClasspathFactory;
import lt.emasina.resthub.model.MdTable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import lt.emasina.resthub.model.MdColumn;
import lt.emasina.resthub.model.MdParameter;
import lt.emasina.resthub.model.MdType;
import org.xml.sax.SAXException;

/**
 * XmlTableFactoryTest
 * @author valdo
 */
@RunWith(JUnit4.class)
public class XmlTableFactoryTest {
    
    private final TableFactory tf;
    
    public XmlTableFactoryTest() throws IOException, SAXException, JAXBException {
        this.tf = new XmlClasspathFactory("lt.emasina.resthub.xml");
    }
    
    private MdTable getTable(String namespace, String name) throws Exception {
        for (MdTable t: tf.getTables()) {
            if (t.getNamespace().equals(namespace) && t.getName().equals(name)) {
                return t;
            }
        }
        return null;
    }
    
    @Test
    public void allXMLTests() throws Exception {
        
        List<MdTable> tables = this.tf.getTables();
        assertTrue(tables.size() > 0);
        
        // sql01.xml
        
        MdTable t = getTable("Lorem", "ipsum");
        assertNull(t);
        
        t = getTable("lorem", "ipsum");
        
        assertNotNull(t);
        assertEquals("lorem", t.getNamespace());
        assertEquals("ipsum", t.getName());
        assertEquals((Integer) 1, t.getHitCount());
        assertEquals((Integer) 0, t.getCacheTime());
        assertEquals((Integer) 30, t.getTimeout());
        assertEquals((Integer) 1000, t.getRowsLimit());
        assertEquals("amet", t.getConnectionName());
        assertEquals((int) 6, t.getMetadata().size());
        assertEquals("lt/emasina/resthub/xml/sql01.xml", t.getMetadata().get("Source"));
        assertEquals("Lorem ipsum dolor sit amet", t.getMetadata().get("sit"));
        assertNotNull(t.getSql());
        assertEquals((int) 1, t.getParameters().size());
        assertEquals((int) 0, t.getColumns().size());
        
        MdParameter p = t.getParameters().get(0);
        assertEquals("param", p.getName());
        assertEquals(MdType.NUMBER, p.getType());
        assertEquals(Boolean.FALSE, p.getArray());
       
        // sql02.xml
        
        t = getTable("wrong", "xml");
        assertNull(t);

        // sql03.xml
        
        t = getTable("complete", "example");
        assertNotNull(t);
        assertEquals("complete", t.getNamespace());
        assertEquals("example", t.getName());
        assertEquals((Integer) 1, t.getHitCount());
        assertEquals((Integer) 120, t.getCacheTime());
        assertEquals((Integer) 30, t.getTimeout());
        assertEquals((Integer) 1000, t.getRowsLimit());
        assertEquals("default", t.getConnectionName());
        assertEquals((int) 2, t.getMetadata().size());
        assertEquals("lt/emasina/resthub/xml/sql03.xml", t.getMetadata().get("Source"));
        assertEquals("Complete example", t.getMetadata().get("description"));
        assertNotNull(t.getSql());
        assertEquals((int) 2, t.getParameters().size());
        assertEquals((int) 2, t.getColumns().size());

        p = t.getParameters().get(0);
        assertEquals("p1", p.getName());
        assertEquals(MdType.NUMBER, p.getType());
        assertEquals(Boolean.FALSE, p.getArray());
        assertEquals((int) 1, p.getMetadata().size());
        assertEquals("metadata", p.getMetadata().get("description"));
        
        p = t.getParameters().get(1);
        assertEquals("p2", p.getName());
        assertEquals(MdType.STRING, p.getType());
        assertEquals(Boolean.TRUE, p.getArray());
        assertEquals((int) 1, p.getMetadata().size());
        assertEquals("metadata", p.getMetadata().get("description"));

        MdColumn c = t.getColumns().get(0);
        assertEquals("c1", c.getName());
        assertEquals(MdType.NUMBER, c.getType());
        assertNull(c.getTable());
        assertEquals((int) 1, c.getMetadata().size());
        assertEquals("metadata", c.getMetadata().get("description"));

        c = t.getColumns().get(1);
        assertEquals("c2", c.getName());
        assertEquals(MdType.STRING, c.getType());
        assertNull(c.getTable());
        assertEquals((int) 1, c.getMetadata().size());
        assertEquals("metadata", c.getMetadata().get("description"));
        
    }
    
}
