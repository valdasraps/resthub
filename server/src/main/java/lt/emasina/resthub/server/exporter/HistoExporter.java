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
package lt.emasina.resthub.server.exporter;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lt.emasina.resthub.server.cache.CcHisto;
import lt.emasina.resthub.server.handler.HistoHandler;
import org.hibernate.Session;

public class HistoExporter extends Exporter<CcHisto> {

    @Inject
    public HistoExporter(@Assisted HistoHandler handler) {
        super(handler);
    }

    @Override
    protected CcHisto retrieveData(Session session) throws Exception {
        return getDf().getHisto(session, (HistoHandler) getHandler());
    }

}
