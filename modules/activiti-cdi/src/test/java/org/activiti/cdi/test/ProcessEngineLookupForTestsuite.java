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
package org.activiti.cdi.test;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import org.activiti.cdi.impl.ProcessEngineLookup;
import org.activiti.engine.ProcessEngine;

/**
 * {@link Alternative} implementation of the {@link ProcessEngineLookup}
 * -interface, providing access to the ProcessEngine built for unit-tests.
 * 
 * @see CdiActivitiTestCase
 * 
 * @author Daniel Meyer
 */
@Alternative
@ApplicationScoped
public class ProcessEngineLookupForTestsuite implements ProcessEngineLookup {

  public static ProcessEngine processEngine;

  @Override
  public ProcessEngine getProcessEngine() {
    return processEngine;
  }
  
  @Override
  public void ungetProcessEngine() {
    // do nothing
  }

}
