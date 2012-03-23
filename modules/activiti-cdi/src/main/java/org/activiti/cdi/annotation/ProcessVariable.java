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
package org.activiti.cdi.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;


/**
 * Annotation for qualifying injection points such that process variables are
 * injected. Requires a process instance to be managed
 * <ul>
 * <li>{@code @Inject @ProcessVariable Object accountNumber}</li>
 * <li>{@code @Inject @ProcessVariable("accountNumber") Object account}</li>
 * </ul>
 * In both cases, the process variable with the name 'accountNumber' is
 * injected. NOTE: injection points must be of type 'object'.
 * <p />
 * 
 * Can also be used to declare bean-properties to hold process variables in
 * combination with the {@link StartProcess} annotation: 
 * <pre>
 * {@code @ProcessVariable }
 * String accountNumber;  // will be added as a process 
 *                        // variable to the 'billingProcess'
 * 
 * {@code @StartProcess("billingProcess")}
 * public void startBillingProcess() {
 *  ...
 * } 
 * </pre>
 * 
 * @author Daniel Meyer
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProcessVariable {

  /**
   * The name of the process variable to look up. Defaults to the name of the
   * annotated field or parameter
   */
  @Nonbinding
  public String value() default "";

}