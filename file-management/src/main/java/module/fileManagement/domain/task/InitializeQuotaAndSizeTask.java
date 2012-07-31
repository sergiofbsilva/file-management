/*
 * @(#)InitializeQuotaAndSizeTask.java
 *
 * Copyright 2011 Instituto Superior Tecnico
 * Founding Authors: Luis Cruz, Sérgio Silva
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the File Management Module.
 *
 *   The File Management Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version 
 *   3 of the License, or (at your option) any later version.
 *
 *   The File Management  Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the File Management  Module. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package module.fileManagement.domain.task;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import pt.ist.bennu.core.domain.scheduler.ReadCustomTask;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.pstm.AbstractDomainObject;
import pt.ist.fenixframework.pstm.Transaction;
import pt.ist.fenixframework.pstm.VersionNotAvailableException;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mysql.jdbc.Connection;

import dml.DomainClass;
import dml.Slot;

/**
 * 
 * @author Sérgio Silva
 * 
 */
public class InitializeQuotaAndSizeTask extends ReadCustomTask {

    @Override
    public void doIt() {
	System.out.println("Starting XXXX");
	out.println("Starting XXXX");
	final Connection connection = (Connection) Transaction.getCurrentJdbcConnection();
	Statement statementQuery = null;
	ResultSet resultSetQuery = null;
	Multimap<String, String> cenas = HashMultimap.create();
	Set<String> oids = new HashSet<String>();
	try {
	    statementQuery = connection.createStatement();
	    resultSetQuery = statementQuery.executeQuery("SELECT COLUMN_NAME, TABLE_NAME \n"
		    + "    FROM INFORMATION_SCHEMA.COLUMNS\n" + "    WHERE COLUMN_NAME LIKE 'OID_%'\n"
		    + "        AND TABLE_SCHEMA='dot';");
	    while (resultSetQuery.next()) {
		final String columnName = resultSetQuery.getString(1);
		final String tableName = resultSetQuery.getString(2);
		cenas.put(tableName, columnName);
	    }

	    statementQuery = connection.createStatement();
	    for (String table : cenas.keySet()) {
		final Collection<String> columns = cenas.get(table);
		final String query = String.format("SELECT %s FROM %s;", StringUtils.join(columns, ","), table);
		System.out.println(query);
		resultSetQuery = statementQuery.executeQuery(query);
		while (resultSetQuery.next()) {
		    for (int i = 1; i <= columns.size(); i++) {
			final String oid = resultSetQuery.getString(i);
			if (!oids.contains(oid)) {
			    oids.add(oid);
			    final DomainObject domainObject = AbstractDomainObject.fromExternalId(oid);
			    if (domainObject != null) {
				final Class clazz = domainObject.getClass();
				final DomainClass domainClass = FenixFramework.getDomainModel().findClass(clazz.getName());
				final Iterator<Slot> slots = domainClass.getSlots();
				if (slots.hasNext()) {
				    invoke(domainObject, domainClass, clazz, slots.next());
				}
			    } else {
				System.out.println("No OID found : " + oid);
			    }
			}
		    }
		}
	    }
	    System.out.println(oids);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private void invoke(final DomainObject domainObject, final DomainClass domainClass, final Class clazz, final Slot slot) {
	String name = StringUtils.EMPTY;
	try {
	    name = "get" + StringUtils.capitalize(slot.getName());
	    Method method = clazz.getMethod(name);
	    method.invoke(domainObject);
	} catch (final InvocationTargetException ex) {
	    if (ex.getCause() != null && ex.getCause() instanceof VersionNotAvailableException) {
		System.out.printf("Found the sob: %s %s %s\n", domainObject.getExternalId(), domainClass.getFullName(), name);
	    }
	} catch (VersionNotAvailableException ex) {
	    System.out.printf(">Found the sob: %s %s %s\n", domainObject.getExternalId(), domainClass.getFullName(), name);
	} catch (Throwable e) {
	    e.printStackTrace();
	}
    }

}
