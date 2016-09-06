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
package org.activiti.engine.impl.bpmn.behavior;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.impl.bpmn.data.AbstractDataAssociation;
import org.activiti.engine.impl.bpmn.data.IOSpecification;
import org.activiti.engine.impl.bpmn.data.ItemInstance;
import org.activiti.engine.impl.bpmn.helper.ErrorPropagation;
import org.activiti.engine.impl.bpmn.webservice.MessageInstance;
import org.activiti.engine.impl.bpmn.webservice.Operation;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

/**
 * An activity behavior that allows calling Web services
 * 
 * @author Esteban Robles Luna
 * @author Falko Menge
 * @author Joram Barrez
 */
public class WebServiceActivityBehavior extends AbstractBpmnActivityBehavior {
  
  public static final String CURRENT_MESSAGE = "org.activiti.engine.impl.bpmn.CURRENT_MESSAGE";

  protected Operation operation;
  
  protected IOSpecification ioSpecification;
  
  protected List<AbstractDataAssociation> dataInputAssociations;

  protected List<AbstractDataAssociation> dataOutputAssociations;

  public WebServiceActivityBehavior() {
    this.dataInputAssociations = new ArrayList<AbstractDataAssociation>();
    this.dataOutputAssociations = new ArrayList<AbstractDataAssociation>();
  }
  
  public void addDataInputAssociation(AbstractDataAssociation dataAssociation) {
    this.dataInputAssociations.add(dataAssociation);
  }
  
  public void addDataOutputAssociation(AbstractDataAssociation dataAssociation) {
    this.dataOutputAssociations.add(dataAssociation);
  }
  
  /**
   * {@inheritDoc}
   */
  public void execute(ActivityExecution execution) throws Exception {
    MessageInstance message;

    try {
      if (ioSpecification != null) {
        this.ioSpecification.initialize(execution);
        ItemInstance inputItem = (ItemInstance) execution
            .getVariable(this.ioSpecification.getFirstDataInputName());
        message = new MessageInstance(this.operation.getInMessage(), inputItem);
      } else {
        message = this.operation.getInMessage().createInstance();
      }

      execution.setVariable(CURRENT_MESSAGE, message);

      this.fillMessage(message, execution);

      ProcessEngineConfigurationImpl processEngineConfig = Context.getProcessEngineConfiguration();
      MessageInstance receivedMessage = this.operation.sendMessage(message,
          processEngineConfig.getWsOverridenEndpointAddresses());

      execution.setVariable(CURRENT_MESSAGE, receivedMessage);

      if (ioSpecification != null) {
        String firstDataOutputName = this.ioSpecification
            .getFirstDataOutputName();
        if (firstDataOutputName != null) {
          ItemInstance outputItem = (ItemInstance) execution
              .getVariable(firstDataOutputName);
          outputItem.getStructureInstance().loadFrom(
              receivedMessage.getStructureInstance().toArray());
        }
      }

      this.returnMessage(receivedMessage, execution);

      execution.setVariable(CURRENT_MESSAGE, null);
      leave(execution);
    } catch (Exception exc) {

      Throwable cause = exc;
      BpmnError error = null;
      while (cause != null) {
        if (cause instanceof BpmnError) {
          error = (BpmnError) cause;
          break;
        }
        cause = cause.getCause();
      }

      if (error != null) {
        ErrorPropagation.propagateError(error, execution);
      } else {
        throw exc;
      }
    }
  }
  
  private void returnMessage(MessageInstance message, ActivityExecution execution) {
    for (AbstractDataAssociation dataAssociation : this.dataOutputAssociations) {
      dataAssociation.evaluate(execution);
    }
  }

  private void fillMessage(MessageInstance message, ActivityExecution execution) {
    for (AbstractDataAssociation dataAssociation : this.dataInputAssociations) {
      dataAssociation.evaluate(execution);
    }
  }

  public void setIoSpecification(IOSpecification ioSpecification) {
    this.ioSpecification = ioSpecification;
  }

  public void setOperation(Operation operation) {
    this.operation = operation;
  }
  
}
