/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package liquibase.ext;

import liquibase.logging.LogLevel;
import liquibase.logging.core.AbstractLogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiquibaseSlf4jLogger extends AbstractLogger {
  
  Logger logger = LoggerFactory.getLogger("liquibase");

  public int getPriority() {
    return 5;
  }

  public void setName(String name) {
    logger = LoggerFactory.getLogger(name);
  }

  public void setLogLevel(String logLevel, String logFile) {
    // ignore
  }

  public void severe(String message) {
    logger.error(message);
  }

  public void severe(String message, Throwable e) {
    logger.error(message, e);
  }

  public void warning(String message) {
    logger.warn(message);
  }

  public void warning(String message, Throwable e) {
    logger.warn(message, e);
  }

  public void info(String message) {
    logger.info(message);
  }

  public void info(String message, Throwable e) {
    logger.info(message, e);
  }

  public void debug(String message) {
    logger.debug(message);
  }

  public void debug(String message, Throwable e) {
    logger.debug(message, e);
  }

  public void setLogLevel(String level) {
  }

  public void setLogLevel(LogLevel level) {
  }

  public LogLevel getLogLevel() {
    return null;
  }
}
