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

import java.net.URL;
import java.util.concurrent.ConcurrentMap;

import javax.xml.namespace.QName;

/**
 * An Operation is part of an {@link BpmnInterface} and it defines Messages that are consumed and (optionally) produced when the Operation is called.
 * 

 */
public class Operation {

  protected String id;

  protected String name;

  protected MessageDefinition inMessage;

  protected MessageDefinition outMessage;

  protected OperationImplementation implementation;

  /**
   * The interface to which this operations belongs
   */
  protected BpmnInterface bpmnInterface;

  public Operation() {

  }

  public Operation(String id, String name, BpmnInterface bpmnInterface, MessageDefinition inMessage) {
    setId(id);
    setName(name);
    setInterface(bpmnInterface);
    setInMessage(inMessage);
  }
  
  public MessageInstance sendMessage(MessageInstance message, ConcurrentMap<QName, URL> overridenEndpointAddresses) throws Exception {
    return this.implementation.sendFor(message, this, overridenEndpointAddresses);
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

  public BpmnInterface getInterface() {
    return bpmnInterface;
  }

  public void setInterface(BpmnInterface bpmnInterface) {
    this.bpmnInterface = bpmnInterface;
  }

  public MessageDefinition getInMessage() {
    return inMessage;
  }

  public void setInMessage(MessageDefinition inMessage) {
    this.inMessage = inMessage;
  }

  public MessageDefinition getOutMessage() {
    return outMessage;
  }

  public void setOutMessage(MessageDefinition outMessage) {
    this.outMessage = outMessage;
  }

  public OperationImplementation getImplementation() {
    return implementation;
  }

  public void setImplementation(OperationImplementation implementation) {
    this.implementation = implementation;
  }
}
