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

package org.activiti.engine.impl.context;

import org.activiti.engine.impl.interceptor.CommandContext;

import java.util.Stack;


/**
 * @author Tom Baeyens
 */
public class Context {

  protected static ThreadLocal<Stack<CommandContext>> commandContextThreadLocal = new ThreadLocal<Stack<CommandContext>>();
  protected static ThreadLocal<Stack<ProcessEngineContext>> processEngineContextStackThreadLocal = new ThreadLocal<Stack<ProcessEngineContext>>();
  protected static ThreadLocal<Stack<ExecutionContext>> executionContextStackThreadLocal = new ThreadLocal<Stack<ExecutionContext>>();
  protected static ThreadLocal<Stack<ProcessDefinitionContext>> processDefinitionContextStackThreadLocal = new ThreadLocal<Stack<ProcessDefinitionContext>>();

  public static CommandContext getCommandContext() {
    return getStack(commandContextThreadLocal).peek();
  }

  public static void setCommandContext(CommandContext commandContext) {
    getStack(commandContextThreadLocal).push(commandContext);
  }

  public static void removeCommandContext() {
    getStack(commandContextThreadLocal).pop();
  }

  public static ProcessEngineContext getProcessEngineContext() {
    return getStack(processEngineContextStackThreadLocal).peek();
  }

  public static void setProcessEngineContext(ProcessEngineContext processEngineContext) {
    getStack(processEngineContextStackThreadLocal).push(processEngineContext);
  }

  public static void removeProcessEngineContext() {
    getStack(processEngineContextStackThreadLocal).pop();
  }

  public static ExecutionContext getExecutionContext() {
    return getStack(executionContextStackThreadLocal).peek();
  }

  public static void setExecutionContext(ExecutionContext executionContext) {
    getStack(executionContextStackThreadLocal).push(executionContext);
  }

  public static void removeExecutionContext() {
    getStack(executionContextStackThreadLocal).pop();
  }

  public static ProcessDefinitionContext getProcessDefinitionContext() {
    return getStack(processDefinitionContextStackThreadLocal).peek();
  }

  public static void setProcessDefinitionContext(ProcessDefinitionContext processDefinitionContext) {
    getStack(processDefinitionContextStackThreadLocal).push(processDefinitionContext);
  }

  public static void removeProcessDefinitionContext() {
    getStack(processDefinitionContextStackThreadLocal).pop();
  }

  protected static <T> Stack<T> getStack(ThreadLocal<Stack<T>> threadLocal) {
    Stack<T> stack = threadLocal.get();
    if (stack==null) {
      stack = new Stack<T>();
      threadLocal.set(stack);
    }
    return stack;
  }
}
