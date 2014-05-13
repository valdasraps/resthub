package net.resthub.server.factory;

import net.resthub.exception.QueryException;
import net.resthub.model.MdTable;
import net.resthub.server.exporter.LobExporter;
import net.resthub.server.exporter.CountExporter;
import net.resthub.server.exporter.DataExporter;
import net.resthub.server.handler.LobHandler;
import net.resthub.server.handler.CountHandler;
import net.resthub.server.handler.DataHandler;
import net.resthub.server.parser.check.CheckExpressionParser;
import net.resthub.server.parser.check.CheckSelectParser;
import net.resthub.server.parser.check.SubSelectDef;
import net.resthub.server.query.Query;
import net.resthub.server.query.QueryId;
import net.resthub.server.table.ServerTable;

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
