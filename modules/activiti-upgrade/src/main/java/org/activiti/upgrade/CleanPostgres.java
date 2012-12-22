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
import java.sql.PreparedStatement;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanPostgres {
  
  private static Logger log = LoggerFactory.getLogger(CleanPostgres.class);
  
  static String[] cleanStatements = new String[] {
  "drop table ACT_GE_PROPERTY cascade;", 
  "drop table ACT_GE_BYTEARRAY cascade;", 
  "drop table ACT_RE_MODEL cascade;", 
  "drop table ACT_RE_DEPLOYMENT cascade;", 
  "drop table ACT_RE_PROCDEF cascade;", 
  "drop table ACT_RU_IDENTITYLINK cascade;", 
  "drop table ACT_RU_VARIABLE cascade;", 
  "drop table ACT_RU_TASK cascade;", 
  "drop table ACT_RU_EXECUTION cascade;", 
  "drop table ACT_RU_JOB cascade;", 
  "drop table ACT_RU_EVENT_SUBSCR cascade;", 
  "drop table ACT_HI_PROCINST cascade;", 
  "drop table ACT_HI_ACTINST cascade;", 
  "drop table ACT_HI_VARINST cascade;", 
  "drop table ACT_HI_TASKINST cascade;", 
  "drop table ACT_HI_DETAIL cascade;", 
  "drop table ACT_HI_COMMENT cascade;", 
  "drop table ACT_HI_ATTACHMENT cascade;", 
  "drop table ACT_ID_INFO cascade;", 
  "drop table ACT_ID_MEMBERSHIP cascade;", 
  "drop table ACT_ID_GROUP cascade;", 
  "drop table ACT_ID_USER cascade;"};

  public static void main(String[] args) {
    if ("postgres".equals(args[0])) {
      CleanPostgres cleanPostgres = new CleanPostgres();
      cleanPostgres.execute();
    }
  }

  public void execute() {
    try {
      ProcessEngineConfigurationImpl processEngineConfiguration = UpgradeUtil.createProcessEngineConfiguration("postgres");
      processEngineConfiguration.buildProcessEngine();
      CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
      commandExecutor.execute(new Command<Object>() {

        public Object execute(CommandContext commandContext) {
          try {
            Connection connection = commandContext.getSession(DbSqlSession.class).getSqlSession().getConnection();
            connection.setAutoCommit(false);

            for (String cleanStatement : cleanStatements) {
              try {
                PreparedStatement preparedStatement = connection.prepareStatement(cleanStatement);
                preparedStatement.execute();
                connection.commit();
                log.info("executed [{}] successfully", cleanStatement);

              } catch (Exception e) {
                log.info("ERROR WHILE EXECUTING [{}]:", cleanStatement, e);
                connection.rollback();
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          return null;
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
