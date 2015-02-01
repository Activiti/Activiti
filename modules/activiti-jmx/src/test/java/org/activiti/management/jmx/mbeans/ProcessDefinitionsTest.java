/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this    except in compliance with the License.
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

package org.activiti.management.jmx.mbeans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBean;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.management.jmx.DefaultManagementMBeanAssembler;
import org.activiti.management.jmx.ManagementMBeanAssembler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Saeid Mirzaei
 */

public class ProcessDefinitionsTest {

  protected ProcessDefinitionsMBean processDefinitionsMBean;

  @Mock
  protected ProcessEngineConfiguration processEngineConfiguration;

  @Mock
  protected RepositoryService repositoryService;

  @Mock
  protected ProcessDefinitionQuery processDefinitionQuery;

  @Mock
  protected DeploymentQuery deploymentQuery;
  
  @Mock
  protected DeploymentBuilder deploymentBuilder;

  protected ManagementMBeanAssembler assembler = new DefaultManagementMBeanAssembler();

  @Before
  public void initMocks() throws MalformedObjectNameException {
    MockitoAnnotations.initMocks(this);
    when(processEngineConfiguration.getRepositoryService()).thenReturn(repositoryService);
    processDefinitionsMBean = new ProcessDefinitionsMBean(processEngineConfiguration);
  }

  @Test
  public void testGetProcessDefinitions() {

    when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);
    List<ProcessDefinition> processDefinitionList = new ArrayList<ProcessDefinition>();
    ProcessDefinitionEntity pd = new ProcessDefinitionEntity();
    pd.setId("testId");
    pd.setName("testName");
    pd.setVersion(175);
    pd.setSuspensionState(1);
    pd.setDescription("testDescription");

    processDefinitionList.add(pd);

    when(processDefinitionQuery.list()).thenReturn(processDefinitionList);

    List<List<String>> result = processDefinitionsMBean.getProcessDefinitions();
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(5, result.get(0).size());
    assertEquals("testId", result.get(0).get(0));
    assertEquals("testName", result.get(0).get(1));
    assertEquals("175", result.get(0).get(2));
    assertEquals("false", result.get(0).get(3));
    assertEquals("testDescription", result.get(0).get(4));

    pd.setSuspensionState(2);

    result = processDefinitionsMBean.getProcessDefinitions();
    assertEquals("true", result.get(0).get(3));

  }

  @Test
  public void testDeployments() {
    when(repositoryService.createDeploymentQuery()).thenReturn(deploymentQuery);
    DeploymentEntity deployment = new DeploymentEntity();
    List<Deployment> deploymentList = new ArrayList<Deployment>();
    deployment.setId("testDeploymentId");
    deployment.setName("testDeploymentName");
    deployment.setTenantId("tenantId");
    deploymentList.add(deployment);
    when(deploymentQuery.list()).thenReturn(deploymentList);

    List<List<String>> result = processDefinitionsMBean.getDeployments();
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(3, result.get(0).size());
    assertEquals("testDeploymentId", result.get(0).get(0));
    assertEquals("testDeploymentName", result.get(0).get(1));
    assertEquals("tenantId", result.get(0).get(2));

  }

  @Test
  public void testDeleteDeployment() {
    processDefinitionsMBean.deleteDeployment("id");
    verify(repositoryService).deleteDeployment("id");
  }

  @Test
  public void testSuspendProcessDefinitionById() {
    processDefinitionsMBean.suspendProcessDefinitionById("id");
    verify(repositoryService).suspendProcessDefinitionById("id");
  }

  @Test
  public void testActivatedProcessDefinitionById() {
    processDefinitionsMBean.activatedProcessDefinitionById("id");
    verify(repositoryService).activateProcessDefinitionById("id");
  }

  @Test
  public void testSuspendProcessDefinitionByKey() {
    processDefinitionsMBean.suspendProcessDefinitionByKey("id");
    verify(repositoryService).suspendProcessDefinitionByKey("id");
  }

  @Test
  public void testActivatedProcessDefinitionByKey() {
    processDefinitionsMBean.activatedProcessDefinitionByKey("id");
    verify(repositoryService).activateProcessDefinitionByKey("id");
  }

  @Test
  public void testAnnotations() throws MalformedObjectNameException, JMException {

    ModelMBean modelBean = assembler.assemble(processDefinitionsMBean, new ObjectName("domain", "key", "value"));
    assertNotNull(modelBean);
    MBeanInfo beanInfo = modelBean.getMBeanInfo();
    assertNotNull(beanInfo);
    assertNotNull(beanInfo.getOperations());
    assertEquals(9, beanInfo.getOperations().length);
    int counter = 0;

    for (MBeanOperationInfo op : beanInfo.getOperations()) {
      if (op.getName().equals("deleteDeployment")) {
        counter++;
        assertEquals("delete deployment", op.getDescription());
        assertEquals("void", op.getReturnType());
        assertEquals(1, op.getSignature().length);
        assertEquals("java.lang.String", op.getSignature()[0].getType());
      } else if (op.getName().equals("suspendProcessDefinitionById")) {
        counter++;
        assertEquals("Suspend given process ID", op.getDescription());
        assertEquals("void", op.getReturnType());
        assertEquals(1, op.getSignature().length);
        assertEquals("java.lang.String", op.getSignature()[0].getType());
      } else if (op.getName().equals("activatedProcessDefinitionById")) {
        counter++;
        assertEquals("Activate given process ID", op.getDescription());
        assertEquals("void", op.getReturnType());
        assertEquals(1, op.getSignature().length);
        assertEquals("java.lang.String", op.getSignature()[0].getType());
      } else if (op.getName().equals("suspendProcessDefinitionByKey")) {
        counter++;
        assertEquals("Suspend given process ID", op.getDescription());
        assertEquals("void", op.getReturnType());
        assertEquals(1, op.getSignature().length);
        assertEquals("java.lang.String", op.getSignature()[0].getType());
      } else if (op.getName().equals("activatedProcessDefinitionByKey")) {
        counter++;
        assertEquals("Activate given process ID", op.getDescription());
        assertEquals("void", op.getReturnType());
        assertEquals(1, op.getSignature().length);
        assertEquals("java.lang.String", op.getSignature()[0].getType());
      } else if (op.getName().equals("deployProcessDefinition")) {
        counter++;
        assertEquals("Deploy Process Definition", op.getDescription());
        assertEquals("void", op.getReturnType());
        assertEquals(2, op.getSignature().length);
        assertEquals("java.lang.String", op.getSignature()[0].getType());
        assertEquals("java.lang.String", op.getSignature()[1].getType());
      }

    }
    assertEquals(6, counter);

    // check attributes
    assertNotNull(beanInfo.getAttributes());
    assertEquals(2, beanInfo.getAttributes().length);

    counter = 0;

    for (MBeanAttributeInfo attr : beanInfo.getAttributes()) {
      if (attr.getName().equals("ProcessDefinitions")) {
        counter++;
        assertEquals("List of Process definitions", attr.getDescription());
        assertEquals("java.util.List", attr.getType());
      } else if (attr.getName().equals("Deployments")) {
        counter++;
        assertEquals("List of deployed Processes", attr.getDescription());
        assertEquals("java.util.List", attr.getType());
      }

    }
    assertEquals(2, counter);
  }

}
