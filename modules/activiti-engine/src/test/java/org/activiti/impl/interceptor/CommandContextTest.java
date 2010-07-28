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

package org.activiti.impl.interceptor;

import static org.junit.Assert.*;

import org.activiti.impl.interceptor.Command;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeployer;
import org.junit.Rule;
import org.junit.Test;


/**
 * @author Tom Baeyens
 */
public class CommandContextTest {

  @Rule
  public LogInitializer logSetup = new LogInitializer();
  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();

  @Test
  public void testOne() {
    try {
      deployer.getCommandExecutor().execute(new Command<Object>() {
        public Object execute(CommandContext commandContext) {
          throw new IllegalStateException("here i come!");
        }
      });
   
      fail("expected exception");
    } catch (IllegalStateException e) {
      // OK
    }
    
    assertNull(CommandContext.getCurrent());
  }
}
