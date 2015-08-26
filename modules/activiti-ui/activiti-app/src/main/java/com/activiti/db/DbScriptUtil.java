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
package com.activiti.db;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DbScriptUtil {
    
    public static void main(String... args) throws Exception {
        dropSchema();
    }

    public static void dropSchema() throws Exception {
        System.out.println("Dropping schema");
        DatabaseConnection databaseConnection = createDbConnection();
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(databaseConnection);
        
        Liquibase liquibase = new Liquibase("META-INF/liquibase/db-changelog-onpremise.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.dropAll();
        
        closeDatabase(database, databaseConnection);
    }
    
    protected static DatabaseConnection createDbConnection() throws Exception {
        Properties properties = new Properties();
        properties.load(DbScriptUtil.class.getClassLoader().getResourceAsStream("META-INF/activiti-app-test/TEST-db.properties"));
        Connection connection = DriverManager.getConnection(properties.getProperty("datasource.url"), 
                properties.getProperty("datasource.username"), properties.getProperty("datasource.password"));
        DatabaseConnection databaseConnection = new JdbcConnection(connection);
        return databaseConnection;
    }
    
    protected static void closeDatabase(Database database, DatabaseConnection databaseConnection) {
        try {
            database.close();
            databaseConnection.close();
        } catch (Exception e) {
            System.out.println("Error closing db connection " + e.getMessage());
        }
    }
}
