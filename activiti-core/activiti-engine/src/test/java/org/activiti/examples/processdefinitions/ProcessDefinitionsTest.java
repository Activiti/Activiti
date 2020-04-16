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
package org.activiti.examples.processdefinitions;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.activiti.engine.impl.bpmn.deployer.ResourceNameUtil;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;

/**
 */
public class ProcessDefinitionsTest extends PluggableActivitiTestCase {

  private static final String NAMESPACE = "xmlns='http://www.omg.org/spec/BPMN/20100524/MODEL'";

  private static final String TARGET_NAMESPACE = "targetNamespace='http://activiti.org/BPMN20'";

  public void testGetProcessDefinitions() {
    List<String> deploymentIds = new ArrayList<String>();
    deploymentIds.add(deployProcessString(("<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <process id='IDR' name='Insurance Damage Report 1' />" + "</definitions>")));
    deploymentIds.add(deployProcessString(("<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <process id='IDR' name='Insurance Damage Report 2' />" + "</definitions>")));
    deploymentIds.add(deployProcessString(("<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <process id='IDR' name='Insurance Damage Report 3' />" + "</definitions>")));
    deploymentIds.add(deployProcessString(("<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <process id='EN' name='Expense Note 1' />" + "</definitions>")));
    deploymentIds.add(deployProcessString(("<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <process id='EN' name='Expense Note 2' />" + "</definitions>")));

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionKey().asc().orderByProcessDefinitionVersion().desc().list();

    assertThat(processDefinitions).isNotNull();

    assertThat(processDefinitions).hasSize(5);

    ProcessDefinition processDefinition = processDefinitions.get(0);
    assertThat(processDefinition.getKey()).isEqualTo("EN");
    assertThat(processDefinition.getName()).isEqualTo("Expense Note 2");
    assertThat(processDefinition.getId().startsWith("EN:2")).isTrue();
    assertThat(processDefinition.getVersion()).isEqualTo(2);

    processDefinition = processDefinitions.get(1);
    assertThat(processDefinition.getKey()).isEqualTo("EN");
    assertThat(processDefinition.getName()).isEqualTo("Expense Note 1");
    assertThat(processDefinition.getId().startsWith("EN:1")).isTrue();
    assertThat(processDefinition.getVersion()).isEqualTo(1);

    processDefinition = processDefinitions.get(2);
    assertThat(processDefinition.getKey()).isEqualTo("IDR");
    assertThat(processDefinition.getName()).isEqualTo("Insurance Damage Report 3");
    assertThat(processDefinition.getId().startsWith("IDR:3")).isTrue();
    assertThat(processDefinition.getVersion()).isEqualTo(3);

    processDefinition = processDefinitions.get(3);
    assertThat(processDefinition.getKey()).isEqualTo("IDR");
    assertThat(processDefinition.getName()).isEqualTo("Insurance Damage Report 2");
    assertThat(processDefinition.getId().startsWith("IDR:2")).isTrue();
    assertThat(processDefinition.getVersion()).isEqualTo(2);

    processDefinition = processDefinitions.get(4);
    assertThat(processDefinition.getKey()).isEqualTo("IDR");
    assertThat(processDefinition.getName()).isEqualTo("Insurance Damage Report 1");
    assertThat(processDefinition.getId().startsWith("IDR:1")).isTrue();
    assertThat(processDefinition.getVersion()).isEqualTo(1);

    Set<String> queryDeploymentIds = new HashSet<String>();
    queryDeploymentIds.add(processDefinitions.get(0).getDeploymentId());
    queryDeploymentIds.add(processDefinitions.get(1).getDeploymentId());
    List<ProcessDefinition> queryProcessDefinitions = repositoryService.createProcessDefinitionQuery().deploymentIds(queryDeploymentIds).orderByProcessDefinitionKey().asc()
        .orderByProcessDefinitionVersion().desc().list();
    assertThat(queryProcessDefinitions).hasSize(2);

    processDefinition = queryProcessDefinitions.get(0);
    assertThat(processDefinition.getKey()).isEqualTo("EN");
    assertThat(processDefinition.getName()).isEqualTo("Expense Note 2");

    processDefinition = queryProcessDefinitions.get(1);
    assertThat(processDefinition.getKey()).isEqualTo("EN");
    assertThat(processDefinition.getName()).isEqualTo("Expense Note 1");

    queryDeploymentIds = new HashSet<String>();
    queryDeploymentIds.add(processDefinitions.get(0).getDeploymentId());
    queryDeploymentIds.add(processDefinitions.get(3).getDeploymentId());
    queryDeploymentIds.add(processDefinitions.get(4).getDeploymentId());
    queryProcessDefinitions = repositoryService.createProcessDefinitionQuery().deploymentIds(queryDeploymentIds).list();
    assertThat(queryProcessDefinitions).hasSize(3);

    processDefinition = queryProcessDefinitions.get(0);
    assertThat(processDefinition.getKey()).isEqualTo("EN");
    assertThat(processDefinition.getName()).isEqualTo("Expense Note 2");

    processDefinition = processDefinitions.get(3);
    assertThat(processDefinition.getKey()).isEqualTo("IDR");
    assertThat(processDefinition.getName()).isEqualTo("Insurance Damage Report 2");

    processDefinition = processDefinitions.get(4);
    assertThat(processDefinition.getKey()).isEqualTo("IDR");
    assertThat(processDefinition.getName()).isEqualTo("Insurance Damage Report 1");

    deleteDeployments(deploymentIds);
  }

  public void testDeployIdenticalProcessDefinitions() {
    List<String> deploymentIds = new ArrayList<String>();
    deploymentIds.add(deployProcessString(("<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <process id='IDR' name='Insurance Damage Report' />" + "</definitions>")));
    deploymentIds.add(deployProcessString(("<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <process id='IDR' name='Insurance Damage Report' />" + "</definitions>")));

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionKey().asc().orderByProcessDefinitionVersion().desc().list();

    assertThat(processDefinitions).isNotNull();
    assertThat(processDefinitions).hasSize(2);

    ProcessDefinition processDefinition = processDefinitions.get(0);
    assertThat(processDefinition.getKey()).isEqualTo("IDR");
    assertThat(processDefinition.getName()).isEqualTo("Insurance Damage Report");
    assertThat(processDefinition.getId().startsWith("IDR:2")).isTrue();
    assertThat(processDefinition.getVersion()).isEqualTo(2);

    processDefinition = processDefinitions.get(1);
    assertThat(processDefinition.getKey()).isEqualTo("IDR");
    assertThat(processDefinition.getName()).isEqualTo("Insurance Damage Report");
    assertThat(processDefinition.getId().startsWith("IDR:1")).isTrue();
    assertThat(processDefinition.getVersion()).isEqualTo(1);

    deleteDeployments(deploymentIds);
  }

  public void testProcessDefinitionDescription() {
    String deploymentId = deployProcessString(("<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <process id='test' name='test'><documentation>This is a test</documentation></process></definitions>"));
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).singleResult();
    assertThat(processDefinition.getDescription()).isEqualTo("This is a test");

    deleteDeployments(singletonList(deploymentId));
  }

  private String deployProcessString(String processString) {
    String resourceName = "xmlString." + ResourceNameUtil.BPMN_RESOURCE_SUFFIXES[0];
    return repositoryService.createDeployment().addString(resourceName, processString).deploy().getId();
  }

  private void deleteDeployments(Collection<String> deploymentIds) {
    for (String deploymentId : deploymentIds) {
      repositoryService.deleteDeployment(deploymentId);
    }
  }
}
