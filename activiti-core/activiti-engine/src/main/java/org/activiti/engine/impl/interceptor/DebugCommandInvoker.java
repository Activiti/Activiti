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
package org.activiti.engine.impl.interceptor;

import org.activiti.engine.debug.ExecutionTreeUtil;
import org.activiti.engine.impl.agenda.AbstractOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**

 */
public class DebugCommandInvoker extends CommandInvoker {
  
  private static final Logger logger = LoggerFactory.getLogger(DebugCommandInvoker.class);
  
  @Override
  public void executeOperation(Runnable runnable) {
    if (runnable instanceof AbstractOperation) {
      AbstractOperation operation = (AbstractOperation) runnable;
      
      if (operation.getExecution() != null) {
        logger.info("Execution tree while executing operation {} :", operation.getClass());
        logger.info("{}", System.lineSeparator() +  ExecutionTreeUtil.buildExecutionTree(operation.getExecution()));
      }
      
    } 
    
    super.executeOperation(runnable);
  }

}
