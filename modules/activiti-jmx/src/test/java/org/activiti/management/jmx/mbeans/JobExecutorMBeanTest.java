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

package org.activiti.management.jmx.mbeans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBean;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.management.jmx.DefaultManagementMBeanAssembler;
import org.activiti.management.jmx.ManagementMBeanAssembler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Saeid Mirzaei
 */

public class JobExecutorMBeanTest {

  protected JobExecutorMBean jobExecutorMbean;

  @Mock
  protected ProcessEngineConfiguration processEngineConfiguration;
  
  @Mock
  protected JobExecutor jobExecutor;

  @Before
  public void initMocks() throws MalformedObjectNameException {
    MockitoAnnotations.initMocks(this);
    when(processEngineConfiguration.getJobExecutor()).thenReturn(jobExecutor);
    jobExecutorMbean = new JobExecutorMBean(processEngineConfiguration);
  }

  @Test
  public void TestIsJobExecutorActivatedFalse() {
    when(jobExecutor.isActive()).thenReturn(false);
   
    boolean result = jobExecutorMbean.isJobExecutorActivated();
    verify(jobExecutor).isActive();
    assertFalse(result);

  }

  @Test
  public void TestIsJobExecutorActivatedTrue() {
    when(jobExecutor.isActive()).thenReturn(true);
    boolean result = jobExecutorMbean.isJobExecutorActivated();
    verify(jobExecutor).isActive();
    assertTrue(result);
  }

  @Test
  public void setJobExecutorActivateTrue() {
    jobExecutorMbean.setJobExecutorActivate(true);
    verify(jobExecutor).start();

    jobExecutorMbean.setJobExecutorActivate(false);
    verify(jobExecutor).shutdown();

  }

  ManagementMBeanAssembler assembler = new DefaultManagementMBeanAssembler();

  @Test
  public void testAnnotations() throws MalformedObjectNameException, JMException {

    ModelMBean modelBean = assembler.assemble(jobExecutorMbean, new ObjectName("domain", "key", "value"));
    assertNotNull(modelBean);
    MBeanInfo beanInfo = modelBean.getMBeanInfo();
    assertNotNull(beanInfo);
    assertNotNull(beanInfo.getOperations());
    assertEquals(2, beanInfo.getOperations().length);
    int counter = 0;

    for (MBeanOperationInfo op : beanInfo.getOperations()) {
      if (op.getName().equals("setJobExecutorActivate")) {
        counter++;
        assertEquals("set job executor activate", op.getDescription());
        assertEquals("void", op.getReturnType());
        assertEquals(1, op.getSignature().length);
        assertEquals("java.lang.Boolean", op.getSignature()[0].getType());
      }
    }
    assertEquals(1, counter);

    // check attributes
    assertNotNull(beanInfo.getAttributes());
    assertEquals(1, beanInfo.getAttributes().length);

    counter = 0;

    for (MBeanAttributeInfo attr : beanInfo.getAttributes()) {
      if (attr.getName().equals("JobExecutorActivated")) {
        counter++;
        assertEquals("check if the job executor is activated", attr.getDescription());
        assertEquals("boolean", attr.getType());
      }
    }
    assertEquals(1, counter);

  }

}
