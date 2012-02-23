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
package org.activiti.engine.impl.jobexecutor;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.cmd.StartProcessInstanceCmd;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.delegate.SignallableActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

import java.util.logging.Level;
import java.util.logging.Logger;


public class TimerStartEventJobHandler implements JobHandler {

  private static Logger log = Logger.getLogger(TimerStartEventJobHandler.class.getName());

  public static final String TYPE = "timer-start-event";

  public String getType() {
    return TYPE;
  }

  public void execute(String configuration, ExecutionEntity execution, CommandContext commandContext) {
    try {
      new StartProcessInstanceCmd(configuration, null, null, null).execute(commandContext);
    } catch (RuntimeException e) {
      log.log(Level.SEVERE, "exception during timer execution", e);
      throw e;
    } catch (Exception e) {
      log.log(Level.SEVERE, "exception during timer execution", e);
      throw new ActivitiException("exception during timer execution: " + e.getMessage(), e);
    }
  }
}
