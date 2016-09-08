/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.app.db;

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
