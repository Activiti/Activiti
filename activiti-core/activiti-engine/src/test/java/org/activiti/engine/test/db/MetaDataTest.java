/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.activiti.engine.test.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MetaDataTest extends PluggableActivitiTestCase {

  private static Logger log = LoggerFactory.getLogger(MetaDataTest.class);

  public void testMetaData() {
    ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration().getCommandExecutor().execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        // PRINT THE TABLE NAMES TO CHECK IF WE CAN USE METADATA INSTEAD
        // THIS IS INTENDED FOR TEST THAT SHOULD RUN ON OUR QA
        // INFRASTRUCTURE TO SEE IF METADATA
        // CAN BE USED INSTEAD OF PERFORMING A QUERY THAT MIGHT FAIL
        try {
          SqlSession sqlSession = commandContext.getDbSqlSession().getSqlSession();
          ResultSet tables = sqlSession.getConnection().getMetaData().getTables(null, null, null, null);
          while (tables.next()) {
            ResultSetMetaData resultSetMetaData = tables.getMetaData();
            int columnCount = resultSetMetaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
              log.info("result set column {}|{}|{}|{}", i, resultSetMetaData.getColumnName(i), resultSetMetaData.getColumnLabel(i), tables.getString(i));
            }
            log.info("-------------------------------------------------------");
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        return null;
      }
    });
  }
}
