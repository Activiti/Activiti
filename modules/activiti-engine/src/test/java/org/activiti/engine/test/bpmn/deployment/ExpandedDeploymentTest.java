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

package org.activiti.engine.test.bpmn.deployment;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import org.activiti.engine.impl.bpmn.deployer.ResourceNameUtilities;
import org.activiti.engine.impl.bpmn.deployer.ExpandedDeployment;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.DeploymentEntityImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntityImpl;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;

import java.util.List;

public class ExpandedDeploymentTest extends PluggableActivitiTestCase {
  private static final String NAMESPACE = "xmlns='http://www.omg.org/spec/BPMN/20100524/MODEL'";
  private static final String TARGET_NAMESPACE = "targetNamespace='http://activiti.org/BPMN20'";
  
  private static final String ID1_ID = "id1";
  private static final String ID2_ID = "id2";
  private static final String IDR_PROCESS_XML = aseembleXmlResourceString(
      "<process id='" + ID1_ID + "' name='Insurance Damage Report 1' />",
      "<process id='" + ID2_ID + "' name='Insurance Damager Report 2' />");
  private static final String IDR_XML_NAME = "idr." + ResourceNameUtilities.BPMN_RESOURCE_SUFFIXES[0];
  
  private static final String EN1_ID = "en1";
  private static final String EN2_ID = "en2";
  private static final String EN_PROCESS_XML = aseembleXmlResourceString(
      "<process id='" + EN1_ID + "' name='Expense Note 1' />",
      "<process id='" + EN2_ID + "' name='Expense Note 2' />");
  private static final String EN_XML_NAME = "en." + ResourceNameUtilities.BPMN_RESOURCE_SUFFIXES[1];
      
  @Override
  public void setUp() {
    Context.setCommandContext(processEngineConfiguration.getCommandContextFactory().createCommandContext(null));
  }
  
  @Override
  public void tearDown() {
    Context.removeCommandContext();
  }
  
  public void testCreateAndQuery() {
    DeploymentEntity entity = assembleUnpersistedDeploymentEntity();

    ExpandedDeployment.BuilderFactory builderFactory = processEngineConfiguration.getExpandedDeploymentBuilderFactory();
    ExpandedDeployment.Builder builder = builderFactory.getBuilderForDeployment(entity);
    ExpandedDeployment expanded = builder.build();
    
    List<ProcessDefinitionEntity> processDefinitions = expanded.getAllProcessDefinitions();
    
    assertThat(expanded.getDeployment(), sameInstance(entity));
    assertThat(processDefinitions.size(), equalTo(4));

    ProcessDefinitionEntity id1 = getProcessDefinitionEntityFromList(processDefinitions, ID1_ID);
    ProcessDefinitionEntity id2 = getProcessDefinitionEntityFromList(processDefinitions, ID2_ID);
    assertThat(expanded.getBpmnParseForProcessDefinition(id1), 
        sameInstance(expanded.getBpmnParseForProcessDefinition(id2)));
    assertThat(expanded.getBpmnModelForProcessDefinition(id1), sameInstance(expanded.getBpmnParseForProcessDefinition(id1).getBpmnModel()));
    assertThat(expanded.getProcessModelForProcessDefinition(id1), sameInstance(expanded.getBpmnParseForProcessDefinition(id1).getBpmnModel().getProcessById(id1.getKey())));
    assertThat(expanded.getResourceForProcessDefinition(id1).getName(), equalTo(IDR_XML_NAME));
    assertThat(expanded.getResourceForProcessDefinition(id2).getName(), equalTo(IDR_XML_NAME));
    
    ProcessDefinitionEntity en1 = getProcessDefinitionEntityFromList(processDefinitions, EN1_ID);
    ProcessDefinitionEntity en2 = getProcessDefinitionEntityFromList(processDefinitions, EN2_ID);
    assertThat(expanded.getBpmnParseForProcessDefinition(en1), 
        sameInstance(expanded.getBpmnParseForProcessDefinition(en2)));
    assertThat(expanded.getBpmnParseForProcessDefinition(en1), not(equalTo(expanded.getBpmnParseForProcessDefinition(id2))));
    assertThat(expanded.getResourceForProcessDefinition(en1).getName(), equalTo(EN_XML_NAME));
    assertThat(expanded.getResourceForProcessDefinition(en2).getName(), equalTo(EN_XML_NAME));
  }
  
  private ProcessDefinitionEntity getProcessDefinitionEntityFromList(List<ProcessDefinitionEntity> list, String idString) {
    for (ProcessDefinitionEntity possible : list) {
      if (possible.getKey().equals(idString)) {
        return possible;
      }
    }
    return null;
  }
  
  private DeploymentEntity assembleUnpersistedDeploymentEntity() {
    DeploymentEntity entity = new DeploymentEntityImpl();
    entity.addResource(buildResource(IDR_XML_NAME, IDR_PROCESS_XML));
    entity.addResource(buildResource(EN_XML_NAME, EN_PROCESS_XML));
    return entity;
  }
  
  private ResourceEntity buildResource(String name, String text) {
    ResourceEntityImpl result = new ResourceEntityImpl();
    result.setName(name);
    result.setBytes(text.getBytes(UTF_8));
    
    return result;
  }
  
  private static String aseembleXmlResourceString(String... definitions) {
    StringBuilder builder = new StringBuilder("<definitions ");
    builder = builder.append(NAMESPACE).append(" ").append(TARGET_NAMESPACE).append(">\n");
    
    for (String definition : definitions) {
      builder = builder.append(definition).append("\n");
    }
    
    builder = builder.append("</definitions>\n");
    
    return builder.toString();
  }
}

