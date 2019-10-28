/*
 * Licensed under the Apache License, Version 2.0 (the "License");
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
 *
 */

package org.activiti.runtime.api.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.activiti.api.process.model.payloads.ReceiveMessagePayload;
import org.activiti.api.process.model.payloads.SetProcessVariablesPayload;
import org.activiti.api.process.model.payloads.SignalPayload;
import org.activiti.api.process.model.payloads.StartMessagePayload;
import org.activiti.api.process.model.payloads.StartProcessPayload;
import org.activiti.common.util.DateFormatterProvider;
import org.activiti.engine.ActivitiException;
import org.activiti.spring.process.model.Extension;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.model.VariableDefinition;
import org.activiti.spring.process.variable.VariableValidationService;

public class ProcessVariablesPayloadValidator  {
   
    private final Map<String, ProcessExtensionModel> processExtensionModelMap;
    private final VariableValidationService variableValidationService;
    private final DateFormatterProvider dateFormatterProvider;
    private final VariableNameValidator variableNameValidator;
 
    public ProcessVariablesPayloadValidator(DateFormatterProvider dateFormatterProvider,
                                            Map<String, ProcessExtensionModel> processExtensionModelMap,
                                            VariableValidationService variableValidationService,
                                            VariableNameValidator variableNameValidator) {
        this.dateFormatterProvider = dateFormatterProvider;
        this.processExtensionModelMap = processExtensionModelMap;
        this.variableValidationService = variableValidationService;
        this.variableNameValidator = variableNameValidator;
    }
    
    private Optional<Map<String, VariableDefinition>> getVariableDefinitionMap(String processDefinitionKey) {
        ProcessExtensionModel processExtensionModel = processDefinitionKey != null ? 
                                                      processExtensionModelMap.get(processDefinitionKey) :
                                                      null;

        return Optional.ofNullable(processExtensionModel)
                .map(ProcessExtensionModel::getExtensions)
                .map(Extension::getProperties);
    }
    
    private boolean validateVariablesAgainstDefinitions(Optional<Map<String, VariableDefinition>> variableDefinitionMap,
                                                        Map.Entry<String, Object> payloadVar,
                                                        Set<String> mismatchedVars) {
        
        if (variableDefinitionMap.isPresent()) {
            
            String name = payloadVar.getKey();
            Object value = payloadVar.getValue();
            
            for (Map.Entry<String, VariableDefinition> variableDefinitionEntry : variableDefinitionMap.get().entrySet()) {
                
                if (variableDefinitionEntry.getValue().getName().equals(name)) {
                    String type = variableDefinitionEntry.getValue().getType();
                 
                    if ("date".equals(type) &&  value != null) {
                        try {
                            payloadVar.setValue(dateFormatterProvider.toDate(value));
                        } catch (Exception e) {
                          //Do nothing here, keep value as a string
                        }
                    } 
                    
                    //Check type 
                    if (!variableValidationService
                            .validateWithErrors(payloadVar.getValue(), variableDefinitionEntry.getValue())
                            .isEmpty()) {
                        
                        mismatchedVars.add(name);
                    }

                    return true;
                }  
            }
        }
        
        return false;       
    }
    
    private void checkPayloadVariables(Map<String, Object> variablePayloadMap,
                                       String processDefinitionKey) {
        
        if (variablePayloadMap == null ) {
            return;
        }
   
        final String errorVariableName = "Variable has not a valid name: {0}";
        final String errorVariableType = "Variables fail type validation: {0}";
        
        final Optional<Map<String, VariableDefinition>> variableDefinitionMap = getVariableDefinitionMap(processDefinitionKey);
        List<ActivitiException> activitiExceptions = new ArrayList<>();
        Set<String> mismatchedVars = new HashSet<>();
        
        for (Map.Entry<String, Object> payloadVar : variablePayloadMap.entrySet()) {
  
                String name = payloadVar.getKey();
                // Check variable name
                if (!variableNameValidator.validate(name)) { 
                    activitiExceptions.add(new ActivitiException(MessageFormat.format(errorVariableName, (name != null ? name : "null" ))));
                } else {
                    
                    boolean found = validateVariablesAgainstDefinitions(variableDefinitionMap,
                                                                        payloadVar,
                                                                        mismatchedVars);
                  
                    if (!found) {           
                        //Try to parse a new string variable as date
                        Object value = payloadVar.getValue();
                        if (value != null && (value instanceof String)) {
                            try {
                                payloadVar.setValue(dateFormatterProvider.toDate(value));
                            } catch (Exception e) {
                                //Do nothing here, keep value as a string
                            }
                        }                 
                    }
            
            }
        }    
        
        if (!mismatchedVars.isEmpty()) {
            activitiExceptions.add(new ActivitiException(MessageFormat.format(errorVariableType, 
                                                                              String.join(", ",
                                                                                          mismatchedVars))));
        } 
        
        if (!activitiExceptions.isEmpty()) {
            throw new IllegalStateException(activitiExceptions.stream()
                                            .map(ex -> ex.getMessage())
                                            .collect(Collectors.joining(",")));                
        }     
    }
    
    public void checkPayloadVariables(SetProcessVariablesPayload setProcessVariablesPayload,
                                      String processDefinitionKey) {
        
        checkPayloadVariables(setProcessVariablesPayload.getVariables(),
                              processDefinitionKey);          
    }
 
    public void checkStartProcessPayloadVariables(StartProcessPayload startProcessPayload,
                                                  String processDefinitionKey) {     
       
        checkPayloadVariables(startProcessPayload.getVariables(),
                             processDefinitionKey);
    }
    
    public void checkStartMessagePayloadVariables(StartMessagePayload startMessagePayload,
                                                  String processDefinitionKey) {
        
       checkPayloadVariables(startMessagePayload.getVariables(),
                             processDefinitionKey);
    }
   
    public void checkReceiveMessagePayloadVariables(ReceiveMessagePayload receiveMessagePayload,
                                                    String processDefinitionKey) {
        
       checkPayloadVariables(receiveMessagePayload.getVariables(),
                             processDefinitionKey);
    }
    
    public void checkSignalPayloadVariables(SignalPayload signalPayload,
                                            String processDefinitionKey) {
        
       checkPayloadVariables(signalPayload.getVariables(),
                             processDefinitionKey);
    }
}
