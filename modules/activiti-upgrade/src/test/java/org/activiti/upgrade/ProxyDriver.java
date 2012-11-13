package org.activiti.upgrade;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class ProxyDriver implements Driver {

  static String url;
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
    if (url.startsWith("jdbc:mysql")) {
      dateFormat = new SimpleDateFormat("''yyyy-MM-dd HH:mm:ss''");
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
