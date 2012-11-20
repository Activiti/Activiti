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
package org.activiti.upgrade;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class ProxyDriver implements Driver {
  
  static String url;
  static DatabaseFormatter databaseFormatter = new DatabaseFormatter();
  static DateFormat dateFormat;

  public static List<String> statements = new ArrayList<String>();
  
  public static void addStatement(String sql) {
    if ( !sql.startsWith("insert into ACT_GE_PROPERTY")
         && !sql.startsWith("update ACT_GE_PROPERTY") 
       ) {
      statements.add(sql+";");
    }
  }
  
  public static void setUrl(String url) {
    ProxyDriver.url = url;
    if (url.startsWith("jdbc:oracle")) {
      databaseFormatter = new DatabaseFormatterOracle();
    } else if (url.startsWith("jdbc:sqlserver")) {
      databaseFormatter = new DatabaseFormatterMsSqlServer();
    } else if (url.startsWith("jdbc:db2")) {
      databaseFormatter = new DatabaseFormatterOracle();
    } else if (url.startsWith("jdbc:postgresql")) {
      databaseFormatter = new DatabaseFormatterOracle();
    } 
  }

  public boolean acceptsURL(String url) throws SQLException {
    return "proxy".equals(url);
  }

  public Connection connect(String url, Properties properties) throws SQLException {
    Connection connection = DriverManager.getConnection(ProxyDriver.url, properties);
    return new ProxyConnection(connection, this);
  }

  public int getMajorVersion() {
    throw new RuntimeException("buzz");
  }

  public int getMinorVersion() {
    throw new RuntimeException("buzz");
  }

  public DriverPropertyInfo[] getPropertyInfo(String arg0, Properties arg1) throws SQLException {
    throw new RuntimeException("buzz");
  }

  public boolean jdbcCompliant() {
    throw new RuntimeException("buzz");
  }

}
