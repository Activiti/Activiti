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
package org.activiti.impl.cfg;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.activiti.ActivitiException;
import org.activiti.Configuration;
import org.activiti.impl.xml.Element;
import org.activiti.impl.xml.Parse;
import org.activiti.impl.xml.Problem;


/**
 * @author Tom Baeyens
 */
public class ConfigurationParse extends Parse {
  
  protected static final List<ConfigurationBinding> bindings = Arrays.asList(
    new ObjectBinding(),
    new StringBinding(),
    new ListBinding(),
    new CommandExecutorBinding(),
    new SqlSessionFactoryBinding(),
    new PersistenceTypeBinding(),
    new TransactionalObjectDescriptorsBinding()
  );

  protected Configuration configuration;
  protected List<Problem> problems = new ArrayList<Problem>();
  protected ConfigurationParser configurationParser;

  public ConfigurationParse(ConfigurationParser configurationParser) {
    super(configurationParser);
    this.configurationParser = configurationParser;
  }
  
  public ConfigurationParse configuration(Configuration configuration) {
    this.configuration = configuration;
    return this;
  }

  @Override
  public ConfigurationParse name(String name) {
    super.name(name);
    return this;
  }

  @Override
  public ConfigurationParse sourceInputStream(InputStream inputStream) {
    super.sourceInputStream(inputStream);
    return this;
  }

  @Override
  public ConfigurationParse sourceResource(String resource, ClassLoader classLoader) {
    super.sourceResource(resource, classLoader);
    return this;
  }

  @Override
  public ConfigurationParse sourceResource(String resource) {
    super.sourceResource(resource);
    return this;
  }

  @Override
  public ConfigurationParse sourceString(String string) {
    super.sourceString(string);
    return this;
  }
  
  @Override
  public ConfigurationParse sourceUrl(URL url) {
    super.sourceUrl(url);
    return this;
  }

  @Override
  public ConfigurationParse sourceUrl(String url) {
    super.sourceUrl(url);
    return this;
  }

  public ConfigurationParse execute() {
    super.execute();

    parseConfiguration();

    if (!problems.isEmpty()) {
      throw new ActivitiException(problems.toString());
    }
    
    return this;
  }

  protected void parseConfiguration() {
    String processManagerFactoryName = rootElement.attribute("name");
    if (processManagerFactoryName!=null) {
      configuration.setName(processManagerFactoryName);
    }
    
    String processManagerFactoryType = rootElement.attribute("type");
    if (processManagerFactoryType!=null) {
      configuration.setType(processManagerFactoryType);
    }
    
    // Handle defaults
    for(ConfigurationBinding binding : bindings) {
      if(binding instanceof DefaultProvidingConfigurationBinding) {
        ((DefaultProvidingConfigurationBinding)binding).supplyDefaults(this);
      }
    }
    
    // Process the main tags
    for (Element element: rootElement.elements()) {
      Object object = parseObject(element);
      if (object!=null) {
        String name = element.attribute("name");
        configuration.configurationObject(name, object);
      }
    }

    for (Element element: rootElement.elements()) {
      if ("import".equals(element.getTagName())) {
        String resource = element.attribute("resource");
        String url = element.attribute("url");
        if (resource!=null) {
          configurationParser.createParse()
            .configuration(configuration)
            .sourceResource(resource)
            .execute();
          
        } else if (url!=null) {
          configurationParser.createParse()
            .configuration(configuration)
            .sourceUrl(url)
            .execute();
        }
      }
    }
  }

  protected ConfigurationBinding getBinding(Element element) {
    for (ConfigurationBinding binding: bindings) {
      if (binding.matches(element, this)) {
        return binding;
      }
    }
    return null;
  }

  protected Object parseObject(Element element) {
    ConfigurationBinding binding = getBinding(element);
    if (binding!=null) {
      return binding.parse(element, this);
    }
    return null;
  }
  
  public void addProblem(String errorMessage, Element element) {
    problems.add(new Problem(errorMessage, name, element));
  }

  public Configuration getConfiguration() {
    return configuration;
  }
}
