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

import org.activiti.engine.cfg.AbstractProcessEngineConfigurator;
import org.activiti.engine.cfg.security.CommandExecutorContext;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;

/**
 * @author Vasile Dirla
 */
public class SecureServiceTaskConfigurator extends AbstractProcessEngineConfigurator {

    protected static ShellCommandExecutorFactory shellCommandExecutorFactory;

    public static ShellCommandExecutorFactory getShellCommandExecutorFactory() {
        return shellCommandExecutorFactory;
    }

    public static void setShellCommandExecutorFactory(ShellCommandExecutorFactory shellCommandExecutorFactory) {
        SecureServiceTaskConfigurator.shellCommandExecutorFactory = shellCommandExecutorFactory;
    }

    protected boolean enableCommandWhiteListing;

    public boolean isEnableCommandWhiteListing() {
        return enableCommandWhiteListing;
    }

    public void setEnableCommandWhiteListing(boolean enableCommandWhiteListing) {
        this.enableCommandWhiteListing = enableCommandWhiteListing;
    }

    @Override
    public void beforeInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        if (shellCommandExecutorFactory == null) {
            shellCommandExecutorFactory = new ShellCommandExecutorFactory();
        }

        if (isEnableCommandWhiteListing() || getWhiteListedShellCommands() != null) {
            shellCommandExecutorFactory.setWhiteListedCommands(getWhiteListedShellCommands());
        }

        CommandExecutorContext.setShellExecutorContextFactory(shellCommandExecutorFactory);
    }

    private Set<String> whitelistedClasses;

    public Set<String> getWhitelistedClasses() {
        return whitelistedClasses;
    }

    public SecureServiceTaskConfigurator setWhitelistedClasses(Set<String> whitelistedClasses) {
        this.whitelistedClasses = whitelistedClasses;
        return this;
    }

    private Set<String> whiteListedShellCommands;

    public Set<String> getWhiteListedShellCommands() {
        return whiteListedShellCommands;
    }

    public SecureServiceTaskConfigurator setWhiteListedShellCommands(Set<String> whiteListedShellCommands) {
        this.whiteListedShellCommands = whiteListedShellCommands;
        return this;
    }
}
