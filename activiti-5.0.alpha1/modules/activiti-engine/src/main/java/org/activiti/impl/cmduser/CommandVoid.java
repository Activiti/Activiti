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
package org.activiti.impl.cmduser;

import org.activiti.ProcessService;


/**
 * Convience class for custom {@link org.activiti.impl.cmduser.Command} types that don't produce a result.
 * 
 * @author Tom Baeyens
 */
public abstract class CommandVoid extends Command<Void> {

  public Void execute(ProcessService processService) throws Exception {
    executeVoid(processService);
    return null;
  }

  public abstract void executeVoid(ProcessService processService) throws Exception;

}
