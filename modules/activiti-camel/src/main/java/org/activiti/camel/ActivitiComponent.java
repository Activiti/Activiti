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
package org.activiti.camel;

import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

/**
 * This class has been modified to be consistent with the changes to CamelBehavior and its implementations. The set of changes
 * significantly increases the flexibility of our Camel integration, as you can either choose one of three "out-of-the-box" modes,
 * or you can choose to create your own. Please reference the comments for the "CamelBehavior" class for more information on the 
 * out-of-the-box implementation class options. 
 * 
 * @author Ryan Johnston (@rjfsu), Tijs Rademakers
 */
public class ActivitiComponent extends DefaultComponent {

  private RuntimeService runtimeService;
  
  private boolean copyVariablesToProperties;

  private boolean copyVariablesToBodyAsMap;

  private boolean copyCamelBodyToBody;

  public ActivitiComponent() {}
  
  @Override
  public void setCamelContext(CamelContext context) {
    super.setCamelContext(context);
    runtimeService = getByType(context, RuntimeService.class);
  }

  private <T> T getByType(CamelContext ctx, Class<T> kls) {
    Map<String, T> looked = ctx.getRegistry().lookupByType(kls);
    if (looked.isEmpty()) {
      return null;
    }
    return looked.values().iterator().next();

  }

  @Override
  protected Endpoint createEndpoint(String s, String s1, Map<String, Object> stringObjectMap) throws Exception {
    ActivitiEndpoint ae = new ActivitiEndpoint(s, getCamelContext(), runtimeService);
    ae.setCopyVariablesToProperties(this.copyVariablesToProperties);
    ae.setCopyVariablesToBodyAsMap(this.copyVariablesToBodyAsMap);
    ae.setCopyCamelBodyToBody(this.copyCamelBodyToBody);
    return ae;
  }
  
  public boolean isCopyVariablesToProperties() {
    return copyVariablesToProperties;
  }
  
  public void setCopyVariablesToProperties(boolean copyVariablesToProperties) {
    this.copyVariablesToProperties = copyVariablesToProperties;
  }
  
  public boolean isCopyCamelBodyToBody() {
    return copyCamelBodyToBody;
  }
  
  public void setCopyCamelBodyToBody(boolean copyCamelBodyToBody) {
    this.copyCamelBodyToBody = copyCamelBodyToBody;
  }
  
  public boolean isCopyVariablesToBodyAsMap() {
    return copyVariablesToBodyAsMap;
  }
  
  public void setCopyVariablesToBodyAsMap(boolean copyVariablesToBodyAsMap) {
    this.copyVariablesToBodyAsMap = copyVariablesToBodyAsMap;
  }
}
