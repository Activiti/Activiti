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
package org.activiti.engine.impl.bpmn;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

/**
 * An activity behavior that allows calling Web services
 * 
 * @author Esteban Robles Luna
 * @author Falko Menge
 */
public class WebServiceActivityBehavior implements ActivityBehavior {
  
  public static final String CURRENT_MESSAGE = "org.activiti.engine.impl.bpmn.CURRENT_MESSAGE";

  protected Operation operation;
  
  protected IOSpecification ioSpecification;
  
  protected List<AbstractDataInputAssociation> dataInputAssociations;

  protected List<AbstractDataOutputAssociation> dataOutputAssociations;

  public WebServiceActivityBehavior(Operation operation) {
    this.operation = operation;
    this.dataInputAssociations = new ArrayList<AbstractDataInputAssociation>();
    this.dataOutputAssociations = new ArrayList<AbstractDataOutputAssociation>();
  }
  
  public void addDataInputAssociation(AbstractDataInputAssociation dataAssociation) {
    this.dataInputAssociations.add(dataAssociation);
  }
  
  public void addDataOutputAssociation(AbstractDataOutputAssociation dataAssociation) {
    this.dataOutputAssociations.add(dataAssociation);
  }
  
  /**
   * {@inheritDoc}
   */
  public void execute(ActivityExecution execution) throws Exception {
    MessageInstance message;
    
    if (ioSpecification != null) {
      this.ioSpecification.initialize(execution);
      ItemInstance inputItem = (ItemInstance) execution.getVariable(this.ioSpecification.getFirstDataInputName());
      message = new MessageInstance(this.operation.getInMessage(), inputItem);
    } else {
      message = this.operation.getInMessage().createInstance();
    }
    
    execution.setVariable(CURRENT_MESSAGE, message);
    
    this.fillMessage(message, execution);
    
    MessageInstance receivedMessage = this.operation.sendMessage(message);

    execution.setVariable(CURRENT_MESSAGE, receivedMessage);

    if (ioSpecification != null) {
      String firstDataOutputName = this.ioSpecification.getFirstDataOutputName();
      if (firstDataOutputName != null) {
        ItemInstance outputItem = (ItemInstance) execution.getVariable(firstDataOutputName);
        outputItem.getStructureInstance().loadFrom(receivedMessage.getStructureInstance().toArray());
      }
    }
    
    this.returnMessage(receivedMessage, execution);
    
    execution.setVariable(CURRENT_MESSAGE, null);
  }
  
  private void returnMessage(MessageInstance message, ActivityExecution execution) {
    for (AbstractDataOutputAssociation dataAssociation : this.dataOutputAssociations) {
      dataAssociation.evaluate(execution);
    }
  }

  private void fillMessage(MessageInstance message, ActivityExecution execution) {
    for (AbstractDataInputAssociation dataAssociation : this.dataInputAssociations) {
      dataAssociation.evaluate(execution);
    }
  }

  public void setIoSpecification(IOSpecification ioSpecification) {
    this.ioSpecification = ioSpecification;
  }
}
