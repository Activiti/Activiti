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

import java.util.List;
import java.util.Set;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.util.ShellCommandExecutor;
import org.activiti.engine.impl.util.ShellExecutorContext;

/**
 * @author Vasile Dirla
 */
public class SecureShellCommandExecutor extends ShellCommandExecutor {

    private Set<String> allowedCommands;

    public SecureShellCommandExecutor(Boolean waitFlag, Boolean cleanEnvBoolan, Boolean redirectErrorFlag, String directoryStr, String resultVariableStr, String errorCodeVariableStr, List<String> argList, Set<String> allowedCommands) {
        super(waitFlag, cleanEnvBoolan, redirectErrorFlag, directoryStr, resultVariableStr, errorCodeVariableStr, argList);
        this.allowedCommands = allowedCommands;
    }

    public SecureShellCommandExecutor(ShellExecutorContext context, Set<String> allowedCommands) {
        super(context);
        this.allowedCommands = allowedCommands;
    }

    @Override
    public void executeCommand(ActivityExecution execution) throws Exception {
        if (getArgList() != null && getArgList().size() > 0) {
            String command = getArgList().get(0);
            if (allowedCommands != null && allowedCommands.contains(command)) {
                super.executeCommand(execution);
            } else {
                throw new RuntimeException("You are not allowed to execute '" + command + "' shell command.");
            }
        }
    }

    public Set<String> getAllowedCommands() {
        return allowedCommands;
    }

    public void setAllowedCommands(Set<String> allowedCommands) {
        this.allowedCommands = allowedCommands;
    }
}
