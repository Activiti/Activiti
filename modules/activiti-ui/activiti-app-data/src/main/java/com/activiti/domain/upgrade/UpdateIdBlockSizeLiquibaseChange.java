/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.activiti.domain.upgrade;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import liquibase.change.custom.CustomSqlChange;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawSqlStatement;

/**
 * This class is a custom changeset for Liquibase.
 * 
 * The issue we saw is that on every reboot, the ids were upped with 32768.
 * This is the default id block size in Hibernate. However, this is done 
 * for every node ... which makes the ids go up very very quickly.
 * 
 * This changeset fixes this, and puts the allocationSize of the id block
 * on 1000. However, we must examine existing data and max id's to know
 * to what value we must put the current block.
 * 
 * @author Joram Barrez
 */
public class UpdateIdBlockSizeLiquibaseChange implements CustomSqlChange {
	
	protected List<String> sqlList = new ArrayList<String>();

	@Override
  public String getConfirmationMessage() {
		String separator = System.getProperty("line.separator");
		StringBuilder strb = new StringBuilder();
		strb.append("Id block size has been updated to 1000 using following sql statements:" + separator);
		for (String sql : sqlList) {
			strb.append(sql + separator);
		}
		return strb.toString();
  }

	@Override
  public void setUp() throws SetupException {
		
  }

	@Override
  public void setFileOpener(ResourceAccessor resourceAccessor) {
		
  }

	@Override
  public ValidationErrors validate(Database database) {
		return new ValidationErrors();
  }

	@Override
  public SqlStatement[] generateStatements(Database database) throws CustomChangeException {
		DatabaseConnection databaseConnection = database.getConnection();
		if (!(databaseConnection instanceof JdbcConnection)) {
			throw new CustomChangeException("Programmatic error: connection is not of type " + JdbcConnection.class);
		}
		
		JdbcConnection jdbcConnection = (JdbcConnection) database.getConnection();
		String[] tables = new String[] { "EMAIL_ACTION", "PROCESS_MODEL", "PROCESS_MODEL_HISTORY", 
				"PROCESS_MODEL_COMMENT", "PROCESS_SHARE_INFO", "USER", "USER_FAVORITE" };
		List<SqlStatement> statements = new ArrayList<SqlStatement>();
		try {
			for (String table : tables) {
				Integer maxId = getMaxIdForTable(jdbcConnection, table);
				// If null, we don't need to insert anything, hibernate will do this automatically later
				if (maxId != null) {
					int maxIdValue = maxId.intValue();
					int newIdBlockValue = Math.round(maxIdValue / 1000) + 100; // + 100K ids just to be sure (multiple nodes can have multiple blocks of 32 locked)
					String sql = "update hibernate_sequences set sequence_next_hi_value=" + newIdBlockValue + " where sequence_name='" + table + "'";
					statements.add(new RawSqlStatement(sql));
					sqlList.add(sql);
				}
			}
			return statements.toArray(new SqlStatement[statements.size()]);
		} catch (Exception e) {
			throw new CustomChangeException("Could not update block size:", e);
		}
  }
	
	protected Integer getMaxIdForTable(JdbcConnection jdbcConnection, String table) throws DatabaseException, SQLException {
		ResultSet resultSet = jdbcConnection.createStatement().executeQuery("select max(id) as max_id from " + table);
		Integer maxId = null;
		while (resultSet.next()) {
			maxId = resultSet.getInt("max_id");
		}
		return maxId;
	}

	
}
