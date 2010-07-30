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
package org.activiti.engine.impl.variable;

import java.io.Serializable;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.el.ActivitiValueExpression;
import org.activiti.pvm.activity.ActivityContext;
import org.activiti.pvm.impl.runtime.ExecutionContextImpl;
import org.activiti.pvm.impl.runtime.ScopeInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class VariableDeclarationImpl implements Serializable {

  private static final long serialVersionUID = 1L;
  
  protected String name;
  protected String type;
  protected String sourceVariableName;
  protected ActivitiValueExpression sourceValueExpression;
  protected String destinationVariableName;
  protected ActivitiValueExpression destinationValueExpression;
  protected String link;
  protected ActivitiValueExpression linkValueExpression;
  
  public void create(ActivityContext activityContext) {
    // TODO evaluate if super scope instance should be exposed on the activity context
    // if that is exposed, make ActivitiValueExpression.getValue(ScopeInstanceImpl) protected again
    ScopeInstanceImpl innerScope = ((ExecutionContextImpl) activityContext).getScopeInstance();
    ScopeInstanceImpl outerScope = innerScope.getParent();

    if (sourceVariableName!=null) {
      if (outerScope.hasVariable(sourceVariableName)) {
        Object value = outerScope.getVariable(sourceVariableName);
        innerScope.setVariable(destinationVariableName, value);      
      } else {
        throw new ActivitiException("Couldn't create variable '" 
                + destinationVariableName + "', since the source variable '"
                + sourceVariableName + "does not exist");
      }
    }
    
    if (sourceValueExpression!=null) {
      Object value = sourceValueExpression.getValue(outerScope);
      innerScope.setVariable(destinationVariableName, value);
    }
    
    if (link!=null) {
      if (outerScope.hasVariable(sourceVariableName)) {
        Object value = outerScope.getVariable(sourceVariableName);
        innerScope.setVariable(destinationVariableName, value);
      } else {
        throw new ActivitiException("Couldn't create variable '" + destinationVariableName + "', since the source variable '" + sourceVariableName
                + "does not exist");
      }
    }

    if (linkValueExpression!=null) {
      Object value = sourceValueExpression.getValue(outerScope);
      innerScope.setVariable(destinationVariableName, value);
    }

  }
  
  public void destroy(ActivityContext activityContext) {
    // TODO evaluate if super scope instance should be exposed on the activity context
    // if that is exposed, make ActivitiValueExpression.getValue(ScopeInstanceImpl) protected again
    ScopeInstanceImpl innerScope = ((ExecutionContextImpl) activityContext).getScopeInstance();
    ScopeInstanceImpl outerScope = innerScope.getParent();
    
    if (destinationVariableName!=null) {
      if (innerScope.hasVariable(sourceVariableName)) {
        Object value = innerScope.getVariable(sourceVariableName);
        outerScope.setVariable(destinationVariableName, value);
      } else {
        throw new ActivitiException("Couldn't destroy variable " + sourceVariableName + ", since it does not exist");
      }
    }

    if (destinationValueExpression!=null) {
      Object value = destinationValueExpression.getValue(innerScope);
      outerScope.setVariable(destinationVariableName, value);
    }
    
    if (link!=null) {
      if (innerScope.hasVariable(sourceVariableName)) {
        Object value = innerScope.getVariable(sourceVariableName);
        outerScope.setVariable(destinationVariableName, value);
      } else {
        throw new ActivitiException("Couldn't destroy variable " + sourceVariableName + ", since it does not exist");
      }
    }

    if (linkValueExpression!=null) {
      Object value = sourceValueExpression.getValue(innerScope);
      outerScope.setVariable(destinationVariableName, value);
    }
  }
  
  public VariableDeclarationImpl(String name, String type) {
    this.name = name;
    this.type = type;
  }

  @Override
  public String toString() {
    return "VariableDeclaration[" + name + ":" + type + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public String getSourceVariableName() {
    return sourceVariableName;
  }
  
  public void setSourceVariableName(String sourceVariableName) {
    this.sourceVariableName = sourceVariableName;
  }

  public ActivitiValueExpression getSourceValueExpression() {
    return sourceValueExpression;
  }
  
  public void setSourceValueExpression(ActivitiValueExpression sourceValueExpression) {
    this.sourceValueExpression = sourceValueExpression;
  }
  
  public String getDestinationVariableName() {
    return destinationVariableName;
  }
  
  public void setDestinationVariableName(String destinationVariableName) {
    this.destinationVariableName = destinationVariableName;
  }

  public ActivitiValueExpression getDestinationValueExpression() {
    return destinationValueExpression;
  }
  
  public void setDestinationValueExpression(ActivitiValueExpression destinationValueExpression) {
    this.destinationValueExpression = destinationValueExpression;
  }
  
  public String getLink() {
    return link;
  }
  
  public void setLink(String link) {
    this.link = link;
  }
  
  public ActivitiValueExpression getLinkValueExpression() {
    return linkValueExpression;
  }

  public void setLinkValueExpression(ActivitiValueExpression linkValueExpression) {
    this.linkValueExpression = linkValueExpression;
  }
}
