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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.impl.bpmn.MessageInstance;
import org.activiti.engine.impl.bpmn.Operation;
import org.activiti.engine.impl.bpmn.OperationImplementation;

/**
 * Represents a WS implementation of a {@link Operation}
 * 
 * @author Esteban Robles Luna
 */
public class WSOperation implements OperationImplementation {

  private static final Logger LOGGER = Logger.getLogger(WSOperation.class.getName());
  
  protected String name;
  
  protected WSService service;
  
  public WSOperation(String operationName, WSService service) {
    this.name = operationName;
    this.service = service;
  }
  
  /**
   * {@inheritDoc}
   */
  public String getName() {
    return this.name;
  }

  /**
   * {@inheritDoc}
   */
  public MessageInstance sendFor(MessageInstance message, Operation operation) {
    Object[] arguments = this.getArguments(message);
    Object[] results = this.safeSend(arguments);
    return this.createResponseMessage(results, operation);
  }

  private Object[] getArguments(MessageInstance message) {
    int fieldSize = message.getFieldSize();
    Object[] arguments = new Object[fieldSize];
    for (int i = 0; i < fieldSize; i++) {
      String fieldName = message.getFieldNameAt(i);
      Object argument = message.getFieldValue(fieldName);
      arguments[i] = argument;
    }
    return arguments;
  }
  
  private Object[] safeSend(Object[] arguments) {
    Object[] results = null;
    
    try {
      results = this.service.getClient().send(this.name, arguments);
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Error calling WS " + this.service.getName(), e);
    }
    
    if (results == null) {
      results = new Object[] {};
    }
    return results;
  }
  
  private MessageInstance createResponseMessage(Object[] results, Operation operation) {
    MessageInstance message = operation.getOutMessage().createInstance();
    
    int fieldSize = message.getFieldSize();
    for (int i = 0; i < fieldSize; i++) {
      String fieldName = message.getFieldNameAt(i);
      Object fieldValue = results[i];
      message.setFieldValue(fieldName, fieldValue);
    }
    
    return message;
  }

  public WSService getService() {
    return this.service;
  }
}
