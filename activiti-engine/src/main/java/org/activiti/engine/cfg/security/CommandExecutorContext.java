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
package org.activiti.engine.cfg.security;

public class CommandExecutorContext {

    private static CommandExecutorFactory shellCommandExecutorFactory;

    public static void setShellExecutorContextFactory(CommandExecutorFactory shellCommandExecutorFactory) {
        CommandExecutorContext.shellCommandExecutorFactory = shellCommandExecutorFactory;
    }

    public static CommandExecutorFactory getShellCommandExecutorFactory() {
        return shellCommandExecutorFactory;
    }
}
