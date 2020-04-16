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
package org.activiti.standalone.escapeclause;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.repository.ProcessDefinitionQuery;

public class ProcessDefinitionQueryEscapeClauseTest extends AbstractEscapeClauseTestCase {

  private String deploymentOneId;
  private String deploymentTwoId;

  @Override
  protected void setUp() throws Exception {
    deploymentOneId = repositoryService
        .createDeployment()
        .tenantId("One%")
        .name("one%")
        .category("testCategory")
        .addClasspathResource("org/activiti/engine/test/repository/one%.bpmn20.xml")
        .deploy()
        .getId();

      deploymentTwoId = repositoryService
        .createDeployment()
        .tenantId("Two_")
        .name("two_")
        .addClasspathResource("org/activiti/engine/test/repository/two_.bpmn20.xml")
        .deploy()
        .getId();

    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    repositoryService.deleteDeployment(deploymentOneId, true);
    repositoryService.deleteDeployment(deploymentTwoId, true);
  }

  public void testQueryByNameLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionNameLike("%\\%%");
    assertThat(query.singleResult().getName()).isEqualTo("One%");
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);

    query = repositoryService.createProcessDefinitionQuery().processDefinitionNameLike("%\\_%");
    assertThat(query.singleResult().getName()).isEqualTo("Two_");
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }

  public void testQueryByCategoryLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionCategoryLike("%\\_%");
    assertThat(query.singleResult().getCategory()).isEqualTo("Examples_");
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }

  public void testQueryByKeyLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionKeyLike("%\\_%");
    assertThat(query.singleResult().getKey()).isEqualTo("two_");
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }

  public void testQueryByResourceNameLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionResourceNameLike("%\\%%");
    assertThat(query.singleResult().getResourceName()).isEqualTo("org/activiti/engine/test/repository/one%.bpmn20.xml");
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);

    query = repositoryService.createProcessDefinitionQuery().processDefinitionResourceNameLike("%\\_%");
    assertThat(query.singleResult().getResourceName()).isEqualTo("org/activiti/engine/test/repository/two_.bpmn20.xml");
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }

  public void testQueryByTenantIdLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionTenantIdLike("%\\%%");
    assertThat(query.singleResult().getTenantId()).isEqualTo("One%");
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);

    query = repositoryService.createProcessDefinitionQuery().processDefinitionTenantIdLike("%\\_%");
    assertThat(query.singleResult().getTenantId()).isEqualTo("Two_");
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);

    query = repositoryService.createProcessDefinitionQuery().latestVersion().processDefinitionTenantIdLike("%\\%%");
    assertThat(query.singleResult().getTenantId()).isEqualTo("One%");
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);

    query = repositoryService.createProcessDefinitionQuery().latestVersion().processDefinitionTenantIdLike("%\\_%");
    assertThat(query.singleResult().getTenantId()).isEqualTo("Two_");
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }
}
