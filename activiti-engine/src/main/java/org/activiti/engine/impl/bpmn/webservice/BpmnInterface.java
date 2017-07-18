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
package org.activiti.engine.impl.bpmn.webservice;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * An Interface defines a set of operations that are implemented by services external to the process.
 * 

 */
public class BpmnInterface {

  protected String id;

  protected String name;

  protected BpmnInterfaceImplementation implementation;

  /**
   * Mapping of the operations of this interface. The key of the map is the id of the operation, for easy retrieval.
   */
  protected Map<String, Operation> operations = new HashMap<String, Operation>();

  public BpmnInterface() {

  }

  public BpmnInterface(String id, String name) {
    setId(id);
    setName(name);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void addOperation(Operation operation) {
    operations.put(operation.getId(), operation);
  }

  public Operation getOperation(String operationId) {
    return operations.get(operationId);
  }

  public Collection<Operation> getOperations() {
    return operations.values();
  }

  public BpmnInterfaceImplementation getImplementation() {
    return implementation;
  }

  public void setImplementation(BpmnInterfaceImplementation implementation) {
    this.implementation = implementation;
  }
}
