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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import org.activiti.engine.runtime.ProcessInstance;

/**
 * Qualifier annotation for injecting the id of the current process instance.
 * <p />
 * Example:
 * 
 * <pre>
 * {@code @Inject} @ProcessInstanceId String pid;
 * </pre>
 * <p />
 * Note that the {@link ProcessInstance} is also available for injection:
 * 
 * <pre>
 * {@code @Inject} ProcessInstance pi;
 * </pre>
 * 
 * @author Daniel Meyer
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE })
public @interface ProcessInstanceId {

}
