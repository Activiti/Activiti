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
package org.activiti.engine.impl.db;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

/**

 */
public class DbSchemaExport {

  public static void main(String[] args) throws Exception {
    if (args == null || args.length != 1) {
      System.err.println("Syntax: java -cp ... org.activiti.engine.impl.db.DbSchemaExport <path-to-properties-file> <path-to-export-file>");
      return;
    }
    File propertiesFile = new File(args[0]);
    if (!propertiesFile.exists()) {
      System.err.println("File '" + args[0] + "' doesn't exist \n" + "Syntax: java -cp ... org.activiti.engine.impl.db.DbSchemaExport <path-to-properties-file> <path-to-export-file>\n");
      return;
    }
    Properties properties = new Properties();
    properties.load(new FileInputStream(propertiesFile));

    String jdbcDriver = properties.getProperty("jdbc.driver");
    String jdbcUrl = properties.getProperty("jdbc.url");
    String jdbcUsername = properties.getProperty("jdbc.username");
    String jdbcPassword = properties.getProperty("jdbc.password");

    Class.forName(jdbcDriver);
    Connection connection = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
    try {
      DatabaseMetaData meta = connection.getMetaData();

      SortedSet<String> tableNames = new TreeSet<String>();
      ResultSet tables = meta.getTables(null, null, null, null);
      while (tables.next()) {
        String tableName = tables.getString(3);
        tableNames.add(tableName);
      }

      System.out.println("TABLES");
      for (String tableName : tableNames) {
        Map<String, String> columnDescriptions = new HashMap<String, String>();
        ResultSet columns = meta.getColumns(null, null, tableName, null);
        while (columns.next()) {
          String columnName = columns.getString(4);
          String columnTypeAndSize = columns.getString(6) + " " + columns.getInt(7);
          columnDescriptions.put(columnName, columnTypeAndSize);
        }

        System.out.println(tableName);
        for (String columnName : new TreeSet<String>(columnDescriptions.keySet())) {
          System.out.println("  " + columnName + " " + columnDescriptions.get(columnName));
        }

        System.out.println("INDEXES");
        SortedSet<String> indexNames = new TreeSet<String>();
        ResultSet indexes = meta.getIndexInfo(null, null, tableName, false, true);
        while (indexes.next()) {
          String indexName = indexes.getString(6);
          indexNames.add(indexName);
        }
        for (String indexName : indexNames) {
          System.out.println(indexName);
        }
        System.out.println();
      }

    } catch (Exception e) {
      e.printStackTrace();
      connection.close();
    }
  }
}
