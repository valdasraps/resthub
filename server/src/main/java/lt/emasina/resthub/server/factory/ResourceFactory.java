/*
 * #%L
 * server
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
package lt.emasina.resthub.server.factory;

import lt.emasina.resthub.TableFactory;
import lt.emasina.resthub.exception.QueryException;
import lt.emasina.resthub.model.MdTable;
import lt.emasina.resthub.server.exporter.HistoExporter;
import lt.emasina.resthub.server.exporter.LobExporter;
import lt.emasina.resthub.server.exporter.CountExporter;
import lt.emasina.resthub.server.exporter.DataExporter;
import lt.emasina.resthub.server.handler.HistoHandler;
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

    public ServerTable create(MdTable table, TableFactory tf);
    public QueryId create(String sql) throws QueryException;
    public Query create(QueryId qid) throws QueryException;
    
    public DataHandler createDataHandler(Query qmd, Form form);
    public CountHandler createCountHandler(Query qmd, Form form);
    public LobHandler createLobHandler(Query qmd, Form form);
    public HistoHandler createHistoHandler(Query qmd, Form form);

    public CountExporter createCountExporter(CountHandler handler);
    public LobExporter createLobExporter(LobHandler handler);
    public DataExporter createDataExporter(DataHandler handler);
    public HistoExporter createHistoExporter(HistoHandler handler);

    public CheckExpressionParser createExpressionParser(SubSelectDef parent);
    public CheckSelectParser createSelectParser(SubSelectDef parent);


}
