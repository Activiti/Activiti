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
package org.activiti.engine.test.bpmn.servicetask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.repository.DeploymentBuilderImpl;
import org.activiti.engine.impl.repository.ResourceEntity;
import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.impl.test.TestHelper;
import org.activiti.engine.impl.util.ClassNameUtil;
import org.activiti.engine.repository.DeploymentBuilder;
import org.mule.api.MuleContext;
import org.mule.api.config.ConfigurationBuilder;
import org.mule.api.context.MuleContextBuilder;
import org.mule.api.context.MuleContextFactory;
import org.mule.config.spring.SpringXmlConfigurationBuilder;
import org.mule.context.DefaultMuleContextBuilder;
import org.mule.context.DefaultMuleContextFactory;

/**
 * @author Esteban Robles Luna
 */
public abstract class AbstractWebServiceTaskTest extends ActivitiInternalTestCase {

  protected MuleContext context;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    MuleContextFactory muleContextFactory = new DefaultMuleContextFactory();
    List<ConfigurationBuilder> builders = new ArrayList<ConfigurationBuilder>();
    builders.add(new SpringXmlConfigurationBuilder("org/activiti/test/mule/mule-cxf-webservice-config.xml"));
    MuleContextBuilder contextBuilder = new DefaultMuleContextBuilder();
    context = muleContextFactory.createMuleContext(builders, contextBuilder);
    context.start();
    
    DeploymentBuilder deploymentBuilder = processEngine.getRepositoryService()
      .createDeployment()
      .name(ClassNameUtil.getClassNameWithoutPackage(this.getClass()) + "." + this.getName());
  
    String resource = TestHelper.getBpmnProcessDefinitionResource(this.getClass(), this.getName());
    deploymentBuilder.addClasspathResource(resource);

    DeploymentBuilderImpl impl = (DeploymentBuilderImpl) deploymentBuilder;
    impl.getDeployment().setValidatingSchema(this.isValidating());
    
    deploymentId = deploymentBuilder.deploy().getId();
  }

  protected boolean isValidating() {
    return true;
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    context.stop();
  }
}
