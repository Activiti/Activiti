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
package org.activiti.engine.impl.delegate.invocation;

import org.activiti.engine.impl.interceptor.DelegateInterceptor;

/**
 * Provides context about the invocation of usercode and handles the actual invocation
 * 

 * @see DelegateInterceptor
 */
public abstract class DelegateInvocation {

  protected Object invocationResult;
  protected Object[] invocationParameters;

  /**
   * make the invocation proceed, performing the actual invocation of the user code.
   * 
   * @throws Exception
   *           the exception thrown by the user code
   */
  public void proceed() {
    invoke();
  }

  protected abstract void invoke();

  /**
   * @return the result of the invocation (can be null if the invocation does not return a result)
   */
  public Object getInvocationResult() {
    return invocationResult;
  }

  /**
   * @return an array of invocation parameters (null if the invocation takes no parameters)
   */
  public Object[] getInvocationParameters() {
    return invocationParameters;
  }

  /**
   * returns the target of the current invocation, ie. JavaDelegate, ValueExpression ...
   */
  public abstract Object getTarget();

}
