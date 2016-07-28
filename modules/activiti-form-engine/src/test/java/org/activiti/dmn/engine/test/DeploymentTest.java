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
package org.activiti.dmn.engine.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.activiti.form.api.Form;
import org.activiti.form.engine.test.FormDeploymentAnnotation;
import org.junit.Test;

public class DeploymentTest extends AbstractActivitiFormTest {

  @Test
  @FormDeploymentAnnotation(resources = "org/activiti/form/engine/test/deployment/simple.form")
  public void deploySingleForm() {
    Form form = repositoryService.createFormQuery()
        .latestVersion()
        .formDefinitionKey("form1")
        .singleResult();
    assertNotNull(form);
    assertEquals("form1", form.getKey());
  }
}
