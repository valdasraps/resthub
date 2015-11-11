package lt.emasina.resthub;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import junit.framework.TestCase;
import lt.emasina.resthub.factory.XmlFolderTableFactory;
import lt.emasina.resthub.model.MdTable;
import org.junit.Test;

public class XmlFolderTableFactoryTest {

    private final Path dir;
    
    public XmlFolderTableFactoryTest() throws IOException {
        this.dir = Paths.get("target/test/tables");
        if (!Files.exists(this.dir)) {
            Files.createDirectories(this.dir);
        }
    }

    private void deleteFile(String fileName) throws IOException {
        Path file = dir.resolve(fileName); 
        Files.deleteIfExists(file);
    }
    
    private void copyFile(String fileName) throws IOException {
        Path src = Paths.get("src/test/resources/lt/emasina/resthub/xml/" + fileName);
        Path tar = dir.resolve(fileName); 
        deleteFile(fileName);
        Files.copy(src, tar);
    }
    
    @Test
    public void simpleTest() throws Exception {
        
        TableFactory tf = new XmlFolderTableFactory(dir.toString());
        
        // First time, add OK file
        
        copyFile("sql01.xml");
        
        TestCase.assertTrue(tf.isRefresh());
        TestCase.assertNull(tf.getNext());
        
        List<MdTable> tables = tf.getTables();
        
        TestCase.assertEquals(2, tables.size());
        TestCase.assertFalse(tf.isRefresh());

        // Second time, add BAD file
        
        Thread.sleep(1000);
        copyFile("sql02.xml");
        
        TestCase.assertTrue(tf.isRefresh());
        
        tables = tf.getTables();
        
        TestCase.assertEquals(2, tables.size());
        TestCase.assertFalse(tf.isRefresh());
        
        // Third time, add OK file
        
        Thread.sleep(1000);
        copyFile("sql03.xml");
        
        TestCase.assertTrue(tf.isRefresh());
        
        tables = tf.getTables();
        
        TestCase.assertEquals(3, tables.size());
        TestCase.assertFalse(tf.isRefresh());

        // Fourth time, remove BAD file
        
        Thread.sleep(1000);
        deleteFile("sql02.xml");
        
        TestCase.assertTrue(tf.isRefresh());
        
        tables = tf.getTables();
        
        TestCase.assertEquals(3, tables.size());
        TestCase.assertFalse(tf.isRefresh());

        // Fifth time, refresh OK file
        
        Thread.sleep(1000);
        copyFile("sql03.xml");
        
        TestCase.assertTrue(tf.isRefresh());
        
        tables = tf.getTables();
        
        TestCase.assertEquals(3, tables.size());
        TestCase.assertFalse(tf.isRefresh());

        // Sixth time, remove OK file
        
        Thread.sleep(1000);
        deleteFile("sql01.xml");
        
        TestCase.assertTrue(tf.isRefresh());
        
        tables = tf.getTables();
        
        TestCase.assertEquals(1, tables.size());
        TestCase.assertFalse(tf.isRefresh());
        
    }
    
}
