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

import org.activiti.engine.impl.context.Context;

/**
 * @author Tom Baeyens
 */
public class CommandInvoker extends AbstractCommandInterceptor {

  @Override
  public <T> T execute(CommandConfig config, Command<T> command) {
    return command.execute(Context.getCommandContext());
  }

  @Override
  public CommandInterceptor getNext() {
    return null;
  }

  @Override
  public void setNext(CommandInterceptor next) {
    throw new UnsupportedOperationException("CommandInvoker must be the last interceptor in the chain");
  }

}
