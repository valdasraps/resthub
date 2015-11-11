package lt.emasina.resthub;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import junit.framework.TestCase;
import lt.emasina.resthub.factory.XmlFileTableFactory;
import lt.emasina.resthub.model.MdTable;
import org.junit.Test;

public class XmlFileTableFactoryTest {

    private final Path dir;
    private final Path tar;
    
    public XmlFileTableFactoryTest() throws IOException {
        this.dir = Paths.get("target/test/table");
        if (!Files.exists(this.dir)) {
            Files.createDirectories(this.dir);
        }
        this.tar = dir.resolve("tables.xml");
    }

    private void deleteFile() throws IOException {
        if (Files.exists(this.tar)) {
            Files.delete(tar);
        }
    }
    
    private void copyFile(String fileName) throws IOException {
        Path src = Paths.get("src/test/resources/lt/emasina/resthub/xml/" + fileName);
        deleteFile();
        Files.copy(src, tar);
        System.out.println(Files.getLastModifiedTime(tar));
    }
    
    @Test
    public void simpleTest() throws Exception {
        
        TableFactory tf = new XmlFileTableFactory(tar.toString());
        
        // First time, OK file
        
        copyFile("sql01.xml");
        
        TestCase.assertTrue(tf.isRefresh());
        TestCase.assertNull(tf.getNext());
        
        List<MdTable> tables = tf.getTables();
        
        TestCase.assertEquals(2, tables.size());
        TestCase.assertFalse(tf.isRefresh());

        // Second time, BAD file
        
        Thread.sleep(1000);
        copyFile("sql02.xml");
        
        TestCase.assertTrue(tf.isRefresh());
        
        tables = tf.getTables();
        
        TestCase.assertNull(tables);
        TestCase.assertFalse(tf.isRefresh());
        
        // Third time, OK file
        
        Thread.sleep(1000);
        copyFile("sql03.xml");
        
        TestCase.assertTrue(tf.isRefresh());
        
        tables = tf.getTables();
        
        TestCase.assertEquals(1, tables.size());
        TestCase.assertFalse(tf.isRefresh());

        // Fourth time, NO file
        
        Thread.sleep(1000);
        deleteFile();
        
        TestCase.assertTrue(tf.isRefresh());
        
        tables = tf.getTables();
        
        TestCase.assertNull(tables);
        TestCase.assertFalse(tf.isRefresh());

        // Fifth time, OK file
        
        Thread.sleep(1000);
        copyFile("sql03.xml");
        
        TestCase.assertTrue(tf.isRefresh());
        
        tables = tf.getTables();
        
        TestCase.assertEquals(1, tables.size());
        TestCase.assertFalse(tf.isRefresh());

    }
    
}
