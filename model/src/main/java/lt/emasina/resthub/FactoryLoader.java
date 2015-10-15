package lt.emasina.resthub;

/**
 * FactoryLoader
 * @author valdo
 */
public interface FactoryLoader {
    
    public ConnectionFactory getConnectionFactory();
    public TableFactory getTableFactory();
    
}
