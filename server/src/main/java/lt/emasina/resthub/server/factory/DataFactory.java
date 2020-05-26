/*
 * #%L
 * server
 * %%
 * Copyright (C) 2012 - 2020 valdasraps
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
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Singleton;

import com.google.common.collect.ImmutableMap;
import lombok.extern.log4j.Log4j;
import lt.emasina.resthub.model.MdColumn;
import lt.emasina.resthub.server.cache.CcHisto;
import lt.emasina.resthub.server.cache.CcLob;
import lt.emasina.resthub.server.cache.CcCount;
import lt.emasina.resthub.server.cache.CcData;
import lt.emasina.resthub.server.exception.ClientErrorException;
import lt.emasina.resthub.server.exception.ServerErrorException;
import lt.emasina.resthub.server.handler.LobHandler;
import lt.emasina.resthub.server.handler.CountHandler;
import lt.emasina.resthub.server.handler.DataHandler;
import lt.emasina.resthub.server.handler.PagedHandler;
import lt.emasina.resthub.server.handler.HistoHandler;
import lt.emasina.resthub.server.query.Query;

import org.apache.commons.lang.text.StrSubstitutor;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.type.BigDecimalType;
import org.hibernate.type.Type;
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
            query.addScalar(c.getName(), c.getType().getHibernateType());
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
            case CLOB:
                query.addScalar(c.getName(), c.getType().getHibernateType());
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

    public CcHisto getHisto(Session session, HistoHandler handler) throws  SQLException {

        final Query q = handler.getQuery();
        final MdColumn column = handler.getColumn();
        final Map<String, Type> columns = handler.getColumns();

        StringBuilder sb = new StringBuilder();
        StrSubstitutor subst;
        switch (column.getType()) {
            case BLOB:
            case CLOB:
                throw new ClientErrorException(Status.CLIENT_ERROR_BAD_REQUEST,
                            "Column %d can not be LOB (%s)!",
                              column.getName(), column.getType().name());
            case STRING:

                subst = new StrSubstitutor(ImmutableMap.of(
                    "sql", q.getSql(),
                    "col", column.getName()));
                sb.append(subst.replace("select ${col}, count(${col}) as count from (${sql}) group by (${col})"));

                if (columns.isEmpty()) {
                    columns.put(column.getName(), column.getType().getHibernateType());
                    columns.put("count", new BigDecimalType());
                }

                break;

            case DATE:
            case NUMBER:

                System.out.println("cia " + handler.getMinValue());
                if (handler.getMinValue() == null || handler.getMaxValue() == null){
                    subst = new StrSubstitutor(ImmutableMap.of(
                            "sql", q.getSql(),
                            "col", column.getName(),
                            "bins", handler.getBins()));

                } else {

                    subst = new StrSubstitutor(ImmutableMap.of(
                        "sql", q.getSql(),
                        "col", column.getName(),
                        "min", handler.getMinValue(),
                        "max", handler.getMaxValue(),
                        "bins", handler.getBins()));

                }


                if(handler.getMinValue() == null && handler.getMaxValue() == null){
                    sb.append(subst.replace("with p___ as ( select null min0___, null max0___, ${bins} steps___ from dual),"));

                } else {
                    sb.append(subst.replace("with p___ as ( select ${min} min0___, ${max} max0___, ${bins} steps___ from dual),"));

                }
                sb.append(subst.replace("q___ (x___) as ( select ${col} from (${sql})),"));
                sb.append(subst.replace("r___ as (select min(min___) as min___, max(max___) as max___, (min(max___) - min(min___)) / min(steps___) as step___,"));
                sb.append(subst.replace(" min(mean___) as mean___, SQRT(sum(POWER(x___ - mean___, 2)) / min(count___)) as rms___ from q___, (select min(nvl(min0___, x___)) as min___, max(nvl(max0___, x___)) as max___,"));
                sb.append(subst.replace(" min(steps___) steps___, avg(x___) as mean___, count(*) as count___ from q___, p___"));
                sb.append(subst.replace(" where (x___ >= min0___ and x___ < max0___) or min0___ is null or max0___ is null))"));
                sb.append(subst.replace("select bnum___ as bin, mean___ as range_mean, rms___ as range_rms, DECODE(bnum___, 0, null, steps___ + 1, null, min___ + (bnum___ - 1) * step___ + step___ / 2) as bin_mean," +
                                               "DECODE(bnum___, 0, null, min___ + (bnum___ - 1) * step___) as bin_from," +
                                               "DECODE(bnum___, steps___ + 1, null, min___ + (bnum___ - 0) * step___) as bin_to, "));
                sb.append(subst.replace("nvl(bin_count___,0) as bin_count from r___, p___, (select rownum - 1 as bnum___ from p___ connect by rownum <= steps___ + 2) left join"));
                sb.append(subst.replace("(select bitem___, count(bitem___) bin_count___ from (select x___, WIDTH_BUCKET(x___, min___, max___, steps___) bitem___ from p___, q___, r___) group by bitem___) on bnum___ = bitem___ order by bnum___ asc"));


                if (columns.isEmpty()) {
                    columns.put("bin", new BigDecimalType());
                    columns.put("bin_mean", column.getType().getHibernateType());
                    columns.put("bin_from", column.getType().getHibernateType());
                    columns.put("bin_to", column.getType().getHibernateType());
                    columns.put("bin_count", new BigDecimalType());
                    columns.put("range_rms", new BigDecimalType());
                    columns.put("range_mean", new BigDecimalType());
                }
        }

        final SQLQuery query = session.createSQLQuery(sb.toString());
        for (Map.Entry<String, Type> e: columns.entrySet()) {
            query.addScalar(e.getKey(), e.getValue());
        }
        
        handler.applyParameters(query);
        if (log.isDebugEnabled()) {
            log.debug(query.getQueryString());
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<CcHisto> func = executor.submit(new Callable<CcHisto>() {

                    @Override
                    public CcHisto call() throws Exception {
                        CcHisto cc = new CcHisto();
                        for (Object o: query.list()) {
                            cc.addRow(q, o);
                        }
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
