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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.activiti.engine.delegate.BusinessRuleTaskDelegate;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.rules.RulesAgendaFilter;
import org.activiti.engine.impl.rules.RulesHelper;
import org.activiti.engine.repository.ProcessDefinition;
import org.drools.KnowledgeBase;
import org.drools.runtime.StatefulKnowledgeSession;


/**
 * activity implementation of the BPMN 2.0 business rule task.
 * 
 * @author Tijs Rademakers
 */
public class BusinessRuleTaskActivityBehavior extends TaskActivityBehavior implements BusinessRuleTaskDelegate {
  
  private static final long serialVersionUID = 1L;
  protected Set<Expression> variablesInputExpressions = new HashSet<Expression>();
  protected Set<Expression> rulesExpressions = new HashSet<Expression>();
  protected boolean exclude = false;
  protected String resultVariable;

  public BusinessRuleTaskActivityBehavior() {}
  
  public void execute(ActivityExecution execution) throws Exception {
    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) execution.getEngineServices().getProcessEngineConfiguration();
    ProcessDefinition processDefinition = processEngineConfiguration.getDeploymentManager().findDeployedProcessDefinitionById(
        execution.getProcessDefinitionId());
    String deploymentId = processDefinition.getDeploymentId();
    
    KnowledgeBase knowledgeBase = RulesHelper.findKnowledgeBaseByDeploymentId(deploymentId); 
    StatefulKnowledgeSession ksession = knowledgeBase.newStatefulKnowledgeSession();
    
    if (variablesInputExpressions != null) {
      Iterator<Expression> itVariable = variablesInputExpressions.iterator();
      while (itVariable.hasNext()) {
        Expression variable = itVariable.next();
        ksession.insert(variable.getValue(execution));
      }
    }
    
    if (!rulesExpressions.isEmpty()) {
      RulesAgendaFilter filter = new RulesAgendaFilter();
      Iterator<Expression> itRuleNames = rulesExpressions.iterator();
      while (itRuleNames.hasNext()) {
        Expression ruleName = itRuleNames.next();
        filter.addSuffic(ruleName.getValue(execution).toString());
      }
      filter.setAccept(!exclude);
      ksession.fireAllRules(filter);
      
    } else {
      ksession.fireAllRules();
    }
    
    Collection<Object> ruleOutputObjects = ksession.getObjects();
    if (ruleOutputObjects != null && !ruleOutputObjects.isEmpty()) {
      Collection<Object> outputVariables = new ArrayList<Object>();
      for (Object object : ruleOutputObjects) {
        outputVariables.add(object);
      }
      execution.setVariable(resultVariable, outputVariables);
    }
    ksession.dispose();
    leave(execution);
  }
  
  public void addRuleVariableInputIdExpression(Expression inputId) {
    this.variablesInputExpressions.add(inputId);
  }
  
  public void addRuleIdExpression(Expression inputId) {
    this.rulesExpressions.add(inputId);
  }
  
  public void setExclude(boolean exclude) {
    this.exclude = exclude;
  }
  
  public void setResultVariable(String resultVariableName) {
    this.resultVariable = resultVariableName;
  }
  
}
