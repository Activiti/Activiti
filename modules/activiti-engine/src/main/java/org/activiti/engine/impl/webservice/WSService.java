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
package org.activiti.engine.impl.webservice;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.bpmn.webservice.BpmnInterface;
import org.activiti.engine.impl.bpmn.webservice.BpmnInterfaceImplementation;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.util.ReflectUtil;

/**
 * Represents a WS implementation of a {@link BpmnInterface}
 * 
 * @author Esteban Robles Luna
 */
public class WSService implements BpmnInterfaceImplementation {

  protected String name;

  protected String location;

  protected Map<String, WSOperation> operations;

  protected String wsdlLocation;

  protected SyncWebServiceClient client;

  public WSService(String name, String location, String wsdlLocation) {
    this.name = name;
    this.location = location;
    this.operations = new HashMap<String, WSOperation>();
    this.wsdlLocation = wsdlLocation;
  }
  
  public WSService(String name, String location, SyncWebServiceClient client) {
    this.name = name;
    this.location = location;
    this.operations = new HashMap<String, WSOperation>();
    this.client = client;
  }

  public void addOperation(WSOperation operation) {
    this.operations.put(operation.getName(), operation);
  }

  SyncWebServiceClient getClient() {
    if (this.client == null) {
      //TODO refactor to use configuration
      SyncWebServiceClientFactory factory = (SyncWebServiceClientFactory) ReflectUtil.instantiate(ProcessEngineConfigurationImpl.DEFAULT_WS_SYNC_FACTORY);
      this.client = factory.create(this.wsdlLocation);
    }
    return this.client;
  }

  /**
   * {@inheritDoc}
   */
  public String getName() {
    return this.name;
  }
  
  public String getLocation() {
    return this.location;
  }
}
