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
package org.activiti.serviceTasks.secure;

import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.cfg.security.CommandExecutorFactory;
import org.activiti.engine.cfg.security.ExecutorContext;
import org.activiti.engine.impl.util.ShellCommandExecutor;
import org.activiti.engine.impl.util.ShellExecutorContext;

/**
 * @author Vasile Dirla
 */
public class ShellCommandExecutorFactory implements CommandExecutorFactory {
    private Set<String> whiteListedCommands;

    public void setWhiteListedCommands(Set<String> whiteListedCommands) {
        this.whiteListedCommands = whiteListedCommands;
    }

    public Set<String> getWhiteListedCommands() {
        return whiteListedCommands;
    }

    @Override
    public ShellCommandExecutor createExecutor(ExecutorContext context) {
        if (context instanceof ShellExecutorContext) {
            return new SecureShellCommandExecutor((ShellExecutorContext) context, whiteListedCommands);
        } else {
            throw new ActivitiException("Invalid context: ExecutorContext !");
        }
    }
}
