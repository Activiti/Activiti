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
package org.activiti.test.db;

import org.activiti.Configuration;
import org.activiti.impl.CmdExecutor;
import org.activiti.impl.ProcessEngineImpl;
import org.activiti.impl.cmd.CmdVoid;
import org.activiti.impl.db.DbidGenerator;
import org.activiti.impl.tx.TransactionContext;
import org.activiti.test.ActivitiTestCase;


/**
 * @author Tom Baeyens
 */
public class DbidGeneratorTest extends ActivitiTestCase {

  public void testDbidGenerator() {
    ProcessEngineImpl processEngineImpl = (ProcessEngineImpl)processEngine;
    CmdExecutor cmdExecutor = processEngineImpl
      .getConfigurationObject(Configuration.NAME_COMMANDEXECUTOR, CmdExecutor.class);

    cmdExecutor.execute(new CmdVoid() {
      public void executeVoid(TransactionContext transactionContext) {
        DbidGenerator dbidGenerator = transactionContext
          .getProcessEngine()
          .getConfigurationObject(Configuration.NAME_DBIDGENERATOR, DbidGenerator.class);
        long firstDbid = dbidGenerator.getNextDbid();
        for (long i=firstDbid+1; i<firstDbid+102; i++ ) {
          assertEquals(i, dbidGenerator.getNextDbid());
        }
      }
    }, processEngineImpl);
  }
}
