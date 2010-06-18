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
package org.activiti.impl.util;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * @author Tom Baeyens
 */
public class LogUtil {

  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  public static void readJavaUtilLoggingConfigFromClasspath() {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream("logging.properties");
    try {
      if (inputStream != null) {
        LogManager.getLogManager().readConfiguration(inputStream);

        String redirectCommons = LogManager.getLogManager().getProperty("redirect.commons.logging");
        if ( (redirectCommons != null) 
             && (!redirectCommons.equalsIgnoreCase("false"))
           ) {
          System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Jdk14Logger" );
        }

        inputStream.close();
      }
    } catch (Exception e) {
      throw new RuntimeException("couldn't initialize logging properly", e);
    }
  }
  
  private static Format dateFormat = new SimpleDateFormat("HH:mm:ss,SSS");
  
  public static class LogFormatter extends Formatter {
    public String format(LogRecord record) {
      StringBuilder line = new StringBuilder();
      line.append(dateFormat.format(new Date()));
      if (Level.FINE.equals(record.getLevel())) {
        line.append(" FIN ");
      } else if (Level.FINEST.equals(record.getLevel())) {
        line.append(" FST ");
      } else if (Level.INFO.equals(record.getLevel())) {
        line.append(" INF ");
      } else if (Level.SEVERE.equals(record.getLevel())) {
        line.append(" SEV ");
      } else if (Level.WARNING.equals(record.getLevel())) {
        line.append(" WRN ");
      } else if (Level.FINER.equals(record.getLevel())) {
        line.append(" FNR ");
      } else if (Level.CONFIG.equals(record.getLevel())) {
        line.append(" CFG ");
      }
      
      line.append(" | ");
      line.append(record.getSourceClassName());
      line.append(" | ");
      line.append(record.getMessage());

      if (record.getThrown()!=null) {
        line.append(LINE_SEPARATOR);
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        record.getThrown().printStackTrace(printWriter);
        line.append(stringWriter.toString());
      }

//      line.append(" | ");
//      line.append(record.getLoggerName());

      line.append(LINE_SEPARATOR);

      return line.toString();
    }
  }
}
