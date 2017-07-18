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

package org.activiti.engine.impl.cfg;

import java.io.InputStream;

import org.activiti.engine.ProcessEngineConfiguration;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

/**

 */
public class BeansConfigurationHelper {

  public static ProcessEngineConfiguration parseProcessEngineConfiguration(Resource springResource, String beanName) {
    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
    xmlBeanDefinitionReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
    xmlBeanDefinitionReader.loadBeanDefinitions(springResource);
    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) beanFactory.getBean(beanName);
    processEngineConfiguration.setBeans(new SpringBeanFactoryProxyMap(beanFactory));
    return processEngineConfiguration;
  }

  public static ProcessEngineConfiguration parseProcessEngineConfigurationFromInputStream(InputStream inputStream, String beanName) {
    Resource springResource = new InputStreamResource(inputStream);
    return parseProcessEngineConfiguration(springResource, beanName);
  }

  public static ProcessEngineConfiguration parseProcessEngineConfigurationFromResource(String resource, String beanName) {
    Resource springResource = new ClassPathResource(resource);
    return parseProcessEngineConfiguration(springResource, beanName);
  }

}
