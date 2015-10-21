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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Singleton;

import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.model.MdColumn;
import lt.emasina.resthub.server.cache.CcLob;
import lt.emasina.resthub.server.cache.CcCount;
import lt.emasina.resthub.server.cache.CcData;
import lt.emasina.resthub.server.exception.ClientErrorException;
import lt.emasina.resthub.server.exception.ServerErrorException;
import lt.emasina.resthub.server.handler.LobHandler;
import lt.emasina.resthub.server.handler.CountHandler;
import lt.emasina.resthub.server.handler.DataHandler;
import lt.emasina.resthub.server.handler.PagedHandler;
import lt.emasina.resthub.server.query.Query;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.type.BigDecimalType;
import org.hibernate.type.BlobType;
import org.hibernate.type.ClobType;
import org.hibernate.type.StringType;
import org.hibernate.type.TextType;
import org.hibernate.type.TimestampType;
import org.hibernate.type.WrapperBinaryType;
import org.restlet.data.Status;

/**
 * DataFactory
 * @author valdo
 */
@Singleton
@Log4j
public class DataFactory {
    
    private static final String START_ROW_PARAM = "START_ROW___";
    private static final String NUM_ROWS_PARAM  = "NUMBER_OF_ROWS___";
    
    public CcData getData(final Session session, final DataHandler handler) throws Exception {     
        final Query q = handler.getQuery();
        final SQLQuery query = getPagedSQLQuery(session, handler);
        
        for (MdColumn c: q.getColumns()) {
            switch (c.getType()) {
                case BLOB:
                    query.addScalar(c.getName(), new BlobType());
                    break;
                case CLOB:
                    query.addScalar(c.getName(), new ClobType());
                    break;
                case DATE:
                    query.addScalar(c.getName(), new TimestampType());
                    break;
                case NUMBER:
                    query.addScalar(c.getName(), new BigDecimalType());
                    break;
                case STRING:
                    query.addScalar(c.getName(), new StringType());
                    break;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(query.getQueryString());
        }
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<CcData> loopRows = executor.submit(
            new Callable<CcData>() {

                @Override
                @SuppressWarnings("unchecked")
                public CcData call() throws Exception {
                    CcData cc = new CcData();
                    for (Object o: query.list()) {
                        cc.addRow(q, o);
                    }
                    return cc;
                };
            });

        try {

            return loopRows.get(q.getTimeOut(), TimeUnit.SECONDS);

        } catch (ExecutionException | InterruptedException ex) {
            throw ex;
        } catch (TimeoutException ex) {
            throw new ServerErrorException(Status.SERVER_ERROR_GATEWAY_TIMEOUT, ex);
        }            
        
    }
    
    public CcLob getLob(final Session session, final LobHandler handler) throws Exception {     
        final Query q = handler.getQuery();
        final SQLQuery query = getPagedSQLQuery(session, handler);
        
        final MdColumn c = handler.getMdColumn();
        switch (c.getType()) {
            case BLOB:
                query.addScalar(c.getName(), new WrapperBinaryType());
                break;
            case CLOB:
                query.addScalar(c.getName(), new TextType());
                break;
            default:
                throw new ClientErrorException(Status.CLIENT_ERROR_BAD_REQUEST, 
                            "Column %d (%s) expected to be LOB found %s", 
                              handler.getColumn(), c.getName(), c.getType().name());
        }

        if (log.isDebugEnabled()) {
            log.debug(query.getQueryString());
        }
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<CcLob> fetchData = executor.submit(
            new Callable<CcLob>() {

                @Override
                @SuppressWarnings("unchecked")
                public CcLob call() throws Exception {
                    CcLob cc = new CcLob();
                    Object o = query.uniqueResult();
                    if (o != null) {
                        switch (c.getType()) {
                            case CLOB:
                                cc.setValue((String) o);
                                break;
                            case BLOB:
                                cc.setValue((Byte[]) o);
                                break;
                        }
                    }
                    return cc;
                };
            });

        try {

            return fetchData.get(q.getTimeOut(), TimeUnit.SECONDS);

        } catch (ExecutionException | InterruptedException ex) {
            throw ex;
        } catch (TimeoutException ex) {
            throw new ServerErrorException(Status.SERVER_ERROR_GATEWAY_TIMEOUT, ex);
        }            
        
    }
    
    private SQLQuery getPagedSQLQuery(final Session session, 
                                      final PagedHandler<?,?> handler) throws SQLException {
        final Query q = handler.getQuery();
        
        Integer perPage = handler.getPerPage();
        Integer page = handler.getPage();
        if (page == null || perPage == null) {
            perPage = q.getRowsLimit();
            page = 1;
        }
        
        Integer startRow = perPage * (page - 1) + 1;
        
        if (handler instanceof LobHandler) {
            startRow = startRow + ((LobHandler) handler).getRow();
            perPage = 1;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("select * from "
                + "  (select ROWNUM ROW_NUMBER___, A.* from (");

        sb.append(q.getSql())
          .append(") A")
          .append("  where ROWNUM < (:").append(START_ROW_PARAM).append(" + :").append(NUM_ROWS_PARAM).append(") ")
          .append(") where ROW_NUMBER___ >= :").append(START_ROW_PARAM);
            
        String sql = sb.toString();
        final SQLQuery query = session.createSQLQuery(sql);
           
        handler.applyParameters(query);

        query.setInteger(START_ROW_PARAM, startRow);
        query.setInteger(NUM_ROWS_PARAM, perPage);
        
        return query;
    }
    
    public CcCount getCount(Session session, CountHandler handler) throws SQLException {
        final Query q = handler.getQuery();
        
        StringBuilder sb = new StringBuilder();
        sb.append("select count(*) from (")
            .append(handler.getQuery().getSql())
            .append(") ");

        String sql = sb.toString();
        
        final SQLQuery query = session.createSQLQuery(sql);
        
        handler.applyParameters(query);
        if (log.isDebugEnabled()) {
            log.debug(query.getQueryString());
        }
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<CcCount> func = executor.submit(
            new Callable<CcCount>() {
                
                @Override
                public CcCount call() throws Exception {
                    CcCount cc = new CcCount();
                    cc.setValue(((BigDecimal) query.uniqueResult()).longValue());
                    return cc;
                }
                
            });
        
            try {
                
                return func.get(q.getTimeOut(), TimeUnit.SECONDS);
                
            } catch (ExecutionException | InterruptedException ex) {
                throw new ServerErrorException(Status.SERVER_ERROR_INTERNAL, ex);
            } catch (TimeoutException ex) {
                throw new ServerErrorException(Status.SERVER_ERROR_GATEWAY_TIMEOUT, ex);
            }
        }
    
}
