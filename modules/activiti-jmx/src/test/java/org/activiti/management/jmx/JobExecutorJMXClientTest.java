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
package org.activiti.management.jmx;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.junit.Test;

/**
 * @author Saeid Mirzaei
 */

public class JobExecutorJMXClientTest {

  @Test
  public void testJobExecutorJMXClient() throws InterruptedException, IOException, MalformedObjectNameException, AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
	String hostName = Utils.getHostName();
	JMXServiceURL url = 
	            new JMXServiceURL("service:jmx:rmi://" + hostName + ":10111/jndi/rmi://" + hostName + ":1099/jmxrmi/activiti");
	   
	    
	ProcessEngineConfiguration processEngineConfig = ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti.cfg.xml");
	processEngineConfig.buildProcessEngine();
	    
	    
	// wait for jmx server to come up
	Thread.sleep(500);
	JMXConnector jmxc = JMXConnectorFactory.connect(url, null);

    MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
    ObjectName jobExecutorBeanName = new ObjectName("org.activiti.jmx.Mbeans:type=JobExecutor");
    
    processEngineConfig.getJobExecutor().shutdown();
    
    // first check that job executor is not activated and correctly reported as being inactive
    assertFalse(processEngineConfig.isJobExecutorActivate());
    assertFalse((Boolean) mbsc.getAttribute(jobExecutorBeanName, "JobExecutorActivated"));
    // now activate it remotely
    mbsc.invoke(jobExecutorBeanName, "setJobExecutorActivate", new Boolean[]{true}, new String[]{Boolean.class.getName()});
    
    // check if it has the effect and correctly reported
//    assertTrue(processEngineConfig.getJobExecutor().isActive());
    assertTrue((Boolean) mbsc.getAttribute(jobExecutorBeanName, "JobExecutorActivated"));
    
    //agani disable and check it    
    mbsc.invoke(jobExecutorBeanName, "setJobExecutorActivate",  new Boolean[]{false}, new String[]{Boolean.class.getName()});
    
    // check if it has the effect and correctly reported
    assertFalse(processEngineConfig.isJobExecutorActivate());
    assertFalse((Boolean) mbsc.getAttribute(jobExecutorBeanName, "JobExecutorActivated"));
  }
}
