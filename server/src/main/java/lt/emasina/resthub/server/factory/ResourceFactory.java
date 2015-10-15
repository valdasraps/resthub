package lt.emasina.resthub.server.factory;

import lt.emasina.resthub.exception.QueryException;
import lt.emasina.resthub.model.MdTable;
import lt.emasina.resthub.server.exporter.LobExporter;
import lt.emasina.resthub.server.exporter.CountExporter;
import lt.emasina.resthub.server.exporter.DataExporter;
import lt.emasina.resthub.server.handler.LobHandler;
import lt.emasina.resthub.server.handler.CountHandler;
import lt.emasina.resthub.server.handler.DataHandler;
import lt.emasina.resthub.server.parser.check.CheckExpressionParser;
import lt.emasina.resthub.server.parser.check.CheckSelectParser;
import lt.emasina.resthub.server.parser.check.SubSelectDef;
import lt.emasina.resthub.server.query.Query;
import lt.emasina.resthub.server.query.QueryId;
import lt.emasina.resthub.server.table.ServerTable;

import org.restlet.data.Form;

/**
 * ResourceMdFactory
 * @author valdo
 */
public interface ResourceFactory {

    public ServerTable create(MdTable table);
    public QueryId create(String sql) throws QueryException;
    public Query create(QueryId qid) throws QueryException;
    
    public DataHandler createDataHandler(Query qmd, Form form);
    public CountHandler createCountHandler(Query qmd, Form form);
    public LobHandler createLobHandler(Query qmd, Form form);
    
    public CountExporter createCountExporter(CountHandler handler);
    public LobExporter createLobExporter(LobHandler handler);
    public DataExporter createDataExporter(DataHandler handler);
    
    public CheckSelectParser createSelectParser(SubSelectDef parent);
    public CheckExpressionParser createExpressionParser(SubSelectDef parent);
    
}
