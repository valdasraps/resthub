package net.resthub.server.test;

import net.sf.jsqlparser.statement.select.Select;

import org.quartz.SchedulerException;

/**
 * SelectParserTestSuite
 * @author valdo
 */
public class UpdateParserTest extends AbstractParserTest {
    
    public UpdateParserTest() throws SchedulerException {}
    
    public void testSimpleSql() {
        Select select = getUpdateSelect("select * from test.customer a");
        assertNull(select.getWithItemsList());
        assertEquals("SELECT * FROM (SELECT * FROM customer) a", select.toString());
    }
    
    public void testSimpleParameterSql() {
        Select s1 = getUpdateSelect("select * from test.customer a where a.id = :id");
        assertEquals("SELECT * FROM (SELECT * FROM customer) a WHERE a.id = :id", s1.toString());
        
        Select s2 = getUpdateSelect("select * from test.customer_with_param a where a.id = :id");
        assertEquals("SELECT * FROM (SELECT * FROM customer WHERE id = :a_id) a WHERE a.id = :id", s2.toString());
    }
    
    public void testSimpleLateralSubselectSql() {
        Select s1 = getUpdateSelect("select * from (select * from test.customer a) b");
        assertEquals("SELECT * FROM (SELECT * FROM (SELECT * FROM customer) a) b", s1.toString());

        Select s2 = getUpdateSelect("select * from (select * from (select * from test.customer a) b join test.customer c) d");
        assertEquals("SELECT * FROM (SELECT * FROM (SELECT * FROM (SELECT * FROM customer) a) b JOIN (SELECT * FROM customer) c) d", s2.toString());
    }
    
    public void testExpressionSubselectSql() {
        Select s1 = getUpdateSelect("select * from (select * from test.customer a) b where b.id in (select c.id from test.customer c)");
        assertEquals("SELECT * FROM (SELECT * FROM (SELECT * FROM customer) a) b WHERE b.id IN (SELECT c.id FROM (SELECT * FROM customer) c)", s1.toString());
        
        Select s2 = getUpdateSelect("select * from (select * from test.customer_with_param a) b where b.id in (select c.id from test.customer_with_param c)");
        assertEquals("SELECT * FROM (SELECT * FROM (SELECT * FROM customer WHERE id = :a_id) a) b WHERE b.id IN (SELECT c.id FROM (SELECT * FROM customer WHERE id = :c_id) c)", s2.toString());
        
        Select s3 = getUpdateSelect("select * from (select * from test.customer_with_param a where a.id = :id) b where b.id in (select c.id from test.customer_with_param c where c.id = :id) and b.id = :id");
        assertEquals("SELECT * FROM (SELECT * FROM (SELECT * FROM customer WHERE id = :a_id) a WHERE a.id = :id) b WHERE b.id IN (SELECT c.id FROM (SELECT * FROM customer WHERE id = :c_id) c WHERE c.id = :id) AND b.id = :id", s3.toString());
    }
    
    public void testParametersSql() {
        getUpdateSelect("select *, :id i, :id1 i1 from test.customer_with_param a join (select * from test.customer_with_param b where b.id = :id) c on a.id = c.id where a.id = :id2");
    }
    
    public void testArrayParamSql() {
        getUpdateSelect("select b.* from test.customer_with_array_param b");
    }
    
}
