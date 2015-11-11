package lt.emasina.resthub;

import java.util.List;
import junit.framework.TestCase;
import lt.emasina.resthub.factory.XmlFileTableFactory;
import lt.emasina.resthub.factory.XmlFolderTableFactory;
import lt.emasina.resthub.model.MdTable;
import org.junit.Test;

public class TableFactoryChainTest {
    
    private class DummyTableFactory extends TableFactory {

            public boolean closed = false;
            
            @Override
            public boolean isRefresh() {
                return false;
            }

            @Override
            public List<MdTable> getTables() throws Exception {
                return null;
            }

            @Override
            public void close() throws Exception {
                closed = true;
            }
        
    }
    
    @Test
    public void simpleTest() throws Exception {
        
        TableFactory tf1 = new DummyTableFactory();
        TableFactory tf2 = new DummyTableFactory();
        
        TableFactory tf = new TableFactory.Builder()
                .add(new XmlFolderTableFactory("dir1"))
                .add(tf2)
                .add(new XmlFileTableFactory("file1"))
                .add(tf1)
                .build();
    
        TestCase.assertNotNull(tf.getNext());
        TestCase.assertNotNull(tf.getNext().getNext());
        TestCase.assertNotNull(tf.getNext().getNext().getNext());
        TestCase.assertNull(tf.getNext().getNext().getNext().getNext());
        
        TestCase.assertFalse(((DummyTableFactory) tf.getNext()).closed);
        TestCase.assertFalse(((DummyTableFactory) tf.getNext().getNext().getNext()).closed);

        tf.closeAll();
        
        TestCase.assertTrue(((DummyTableFactory) tf.getNext()).closed);
        TestCase.assertTrue(((DummyTableFactory) tf.getNext().getNext().getNext()).closed);
        
    }
    
}
