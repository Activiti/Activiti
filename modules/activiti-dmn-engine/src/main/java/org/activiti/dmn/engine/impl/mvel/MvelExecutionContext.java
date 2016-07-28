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
package org.activiti.dmn.engine.impl.mvel;

import java.util.HashMap;
import java.util.Map;

import org.activiti.dmn.api.DecisionExecutionAuditContainer;
import org.activiti.dmn.engine.ActivitiDmnException;
import org.apache.commons.lang3.StringUtils;
import org.mvel2.ParserContext;
import org.mvel2.integration.PropertyHandler;

/**
 * @author Yvo Swillens
 */
public class MvelExecutionContext {

    protected Map<String, Object> resultVariables = new HashMap<String, Object>();
    protected Map<String, Object> stackVariables = null;
    protected ParserContext parserContext = null;
    protected Map<Class<?>, PropertyHandler> propertyHandlers = new HashMap<Class<?>, PropertyHandler>();
    protected DecisionExecutionAuditContainer auditContainer = null;

    public void checkExecutionContext(String variableId) {

        if (StringUtils.isEmpty(variableId)) {
            throw new IllegalArgumentException("Variable id cannot be empty");
        }

        if (stackVariables == null || stackVariables.isEmpty()) {
            throw new IllegalArgumentException("Variables cannot be empty when variable id: "+variableId+" is used");
        }

        if (variableId.contains(".")) {
            String rootVariableId = variableId.substring(0, variableId.indexOf("."));
            if (!stackVariables.containsKey(rootVariableId)) {
                throw new ActivitiDmnException("referred id: " + rootVariableId + " is not present on the context");
            }
            
        } else if (!stackVariables.containsKey(variableId)) {
            throw new ActivitiDmnException("referred id: " + variableId + " is not present on the context");
        }
    }

    public void addResultVariable(String key, Object value) {
        resultVariables.put(key, value);
    }

    public void setStackVariables(Map<String, Object> variables) {
        this.stackVariables = variables;
    }

    public Map<String, Object> getStackVariables() {
        return stackVariables;
    }

    public Map<String, Object> getResultVariables() {
        return resultVariables;
    }

    public ParserContext getParserContext(){
        return parserContext;
    }

    public void setParserContext(ParserContext parserContext) {
        this.parserContext = parserContext;
    }
    
    public Map<Class<?>, PropertyHandler> getPropertyHandlers() {
        return propertyHandlers;
    }
    
    public void addPropertyHandler(Class<?> variableClass, PropertyHandler propertyHandler) {
        propertyHandlers.put(variableClass, propertyHandler);
    }

    public DecisionExecutionAuditContainer getAuditContainer() {
        return auditContainer;
    }

    public void setAuditContainer(DecisionExecutionAuditContainer auditContainer) {
        this.auditContainer = auditContainer;
    }
}