/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.bpmn.behavior;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.DataAssociation;
import org.activiti.bpmn.model.DataSpec;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.IOSpecification;
import org.activiti.bpmn.model.Import;
import org.activiti.bpmn.model.Interface;
import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.SendTask;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.data.AbstractDataAssociation;
import org.activiti.engine.impl.bpmn.data.Assignment;
import org.activiti.engine.impl.bpmn.data.ClassStructureDefinition;
import org.activiti.engine.impl.bpmn.data.ItemDefinition;
import org.activiti.engine.impl.bpmn.data.ItemInstance;
import org.activiti.engine.impl.bpmn.data.ItemKind;
import org.activiti.engine.impl.bpmn.data.SimpleDataInputAssociation;
import org.activiti.engine.impl.bpmn.data.StructureDefinition;
import org.activiti.engine.impl.bpmn.data.TransformationDataOutputAssociation;
import org.activiti.engine.impl.bpmn.helper.ErrorPropagation;
import org.activiti.engine.impl.bpmn.parser.XMLImporter;
import org.activiti.engine.impl.bpmn.webservice.BpmnInterface;
import org.activiti.engine.impl.bpmn.webservice.MessageDefinition;
import org.activiti.engine.impl.bpmn.webservice.MessageImplicitDataInputAssociation;
import org.activiti.engine.impl.bpmn.webservice.MessageImplicitDataOutputAssociation;
import org.activiti.engine.impl.bpmn.webservice.MessageInstance;
import org.activiti.engine.impl.bpmn.webservice.Operation;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.webservice.WSOperation;
import org.activiti.engine.impl.webservice.WSService;
import org.apache.commons.lang3.StringUtils;

/**
 * An activity behavior that allows calling Web services
 *



 */
public class WebServiceActivityBehavior extends AbstractBpmnActivityBehavior {

  private static final long serialVersionUID = 1L;

  public static final String CURRENT_MESSAGE = "org.activiti.engine.impl.bpmn.CURRENT_MESSAGE";

  protected Map<String, XMLImporter> xmlImporterMap = new HashMap<String, XMLImporter>();
  protected Map<String, WSOperation> wsOperationMap = new HashMap<String, WSOperation>();
  protected Map<String, StructureDefinition> structureDefinitionMap = new HashMap<String, StructureDefinition>();
  protected Map<String, WSService> wsServiceMap = new HashMap<String, WSService>();
  protected Map<String, Operation> operationMap = new HashMap<String, Operation>();
  protected Map<String, ItemDefinition> itemDefinitionMap = new HashMap<String, ItemDefinition>();
  protected Map<String, MessageDefinition> messageDefinitionMap = new HashMap<String, MessageDefinition>();

  public WebServiceActivityBehavior() {
    itemDefinitionMap.put("http://www.w3.org/2001/XMLSchema:string", new ItemDefinition("http://www.w3.org/2001/XMLSchema:string", new ClassStructureDefinition(String.class)));
  }

  public void execute(DelegateExecution execution) {
    BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(execution.getProcessDefinitionId());
    FlowElement flowElement = execution.getCurrentFlowElement();

    IOSpecification ioSpecification = null;
    String operationRef = null;
    List<DataAssociation> dataInputAssociations = null;
    List<DataAssociation> dataOutputAssociations = null;

    if (flowElement instanceof SendTask) {
      SendTask sendTask = (SendTask) flowElement;
      ioSpecification = sendTask.getIoSpecification();
      operationRef = sendTask.getOperationRef();
      dataInputAssociations = sendTask.getDataInputAssociations();
      dataOutputAssociations = sendTask.getDataOutputAssociations();

    } else if (flowElement instanceof ServiceTask) {
      ServiceTask serviceTask = (ServiceTask) flowElement;
      ioSpecification = serviceTask.getIoSpecification();
      operationRef = serviceTask.getOperationRef();
      dataInputAssociations = serviceTask.getDataInputAssociations();
      dataOutputAssociations = serviceTask.getDataOutputAssociations();

    } else {
      throw new ActivitiException("Unsupported flow element type " + flowElement);
    }

    MessageInstance message = null;

    fillDefinitionMaps(bpmnModel);

    Operation operation = operationMap.get(operationRef);

    try {

      if (ioSpecification != null) {
        initializeIoSpecification(ioSpecification, execution, bpmnModel);
        if (ioSpecification.getDataInputRefs().size() > 0) {
          String firstDataInputName = ioSpecification.getDataInputRefs().get(0);
          ItemInstance inputItem = (ItemInstance) execution.getVariable(firstDataInputName);
          message = new MessageInstance(operation.getInMessage(), inputItem);
        }

      } else {
        message = operation.getInMessage().createInstance();
      }

      execution.setVariable(CURRENT_MESSAGE, message);

      fillMessage(dataInputAssociations, execution);

      ProcessEngineConfigurationImpl processEngineConfig = Context.getProcessEngineConfiguration();
      MessageInstance receivedMessage = operation.sendMessage(message,
          processEngineConfig.getWsOverridenEndpointAddresses());

      execution.setVariable(CURRENT_MESSAGE, receivedMessage);

      if (ioSpecification != null && ioSpecification.getDataOutputRefs().size() > 0) {
        String firstDataOutputName = ioSpecification.getDataOutputRefs().get(0);
        if (firstDataOutputName != null) {
          ItemInstance outputItem = (ItemInstance) execution.getVariable(firstDataOutputName);
          outputItem.getStructureInstance().loadFrom(receivedMessage.getStructureInstance().toArray());
        }
      }

      returnMessage(dataOutputAssociations, execution);

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
      } else if (exc instanceof RuntimeException){
         throw (RuntimeException) exc;
      }
   }
  }

  protected void initializeIoSpecification(IOSpecification activityIoSpecification, DelegateExecution execution, BpmnModel bpmnModel) {

    for (DataSpec dataSpec : activityIoSpecification.getDataInputs()) {
      ItemDefinition itemDefinition = itemDefinitionMap.get(dataSpec.getItemSubjectRef());
      execution.setVariable(dataSpec.getId(), itemDefinition.createInstance());
    }

    for (DataSpec dataSpec : activityIoSpecification.getDataOutputs()) {
      ItemDefinition itemDefinition = itemDefinitionMap.get(dataSpec.getItemSubjectRef());
      execution.setVariable(dataSpec.getId(), itemDefinition.createInstance());
    }
  }

  protected void fillDefinitionMaps(BpmnModel bpmnModel) {

    for (Import theImport : bpmnModel.getImports()) {
      fillImporterInfo(theImport, bpmnModel.getSourceSystemId());
    }

    createItemDefinitions(bpmnModel);
    createMessages(bpmnModel);
    createOperations(bpmnModel);
  }

  protected void createItemDefinitions(BpmnModel bpmnModel) {

    for (org.activiti.bpmn.model.ItemDefinition itemDefinitionElement : bpmnModel.getItemDefinitions().values()) {

      if (!itemDefinitionMap.containsKey(itemDefinitionElement.getId())) {
        StructureDefinition structure = null;

        try {
          // it is a class
          Class<?> classStructure = ReflectUtil.loadClass(itemDefinitionElement.getStructureRef());
          structure = new ClassStructureDefinition(classStructure);
        } catch (ActivitiException e) {
          // it is a reference to a different structure
          structure = structureDefinitionMap.get(itemDefinitionElement.getStructureRef());
        }

        ItemDefinition itemDefinition = new ItemDefinition(itemDefinitionElement.getId(), structure);
        if (StringUtils.isNotEmpty(itemDefinitionElement.getItemKind())) {
          itemDefinition.setItemKind(ItemKind.valueOf(itemDefinitionElement.getItemKind()));
        }

        itemDefinitionMap.put(itemDefinition.getId(), itemDefinition);
      }
    }
  }

  public void createMessages(BpmnModel bpmnModel) {
    for (Message messageElement : bpmnModel.getMessages()) {
      if (!messageDefinitionMap.containsKey(messageElement.getId())) {
        MessageDefinition messageDefinition = new MessageDefinition(messageElement.getId());
        if (StringUtils.isNotEmpty(messageElement.getItemRef())) {
          if (itemDefinitionMap.containsKey(messageElement.getItemRef())) {
            ItemDefinition itemDefinition = itemDefinitionMap.get(messageElement.getItemRef());
            messageDefinition.setItemDefinition(itemDefinition);
          }
        }

        messageDefinitionMap.put(messageDefinition.getId(), messageDefinition);
      }
    }
  }

  protected void createOperations(BpmnModel bpmnModel) {
    for (Interface interfaceObject : bpmnModel.getInterfaces()) {
      BpmnInterface bpmnInterface = new BpmnInterface(interfaceObject.getId(), interfaceObject.getName());
      bpmnInterface.setImplementation(wsServiceMap.get(interfaceObject.getImplementationRef()));

      for (org.activiti.bpmn.model.Operation operationObject : interfaceObject.getOperations()) {

        if (!operationMap.containsKey(operationObject.getId())) {
          MessageDefinition inMessage = messageDefinitionMap.get(operationObject.getInMessageRef());
          Operation operation = new Operation(operationObject.getId(), operationObject.getName(), bpmnInterface, inMessage);
          operation.setImplementation(wsOperationMap.get(operationObject.getImplementationRef()));

          if (StringUtils.isNotEmpty(operationObject.getOutMessageRef())) {
            if (messageDefinitionMap.containsKey(operationObject.getOutMessageRef())) {
              MessageDefinition outMessage = messageDefinitionMap.get(operationObject.getOutMessageRef());
              operation.setOutMessage(outMessage);
            }
          }

          operationMap.put(operation.getId(), operation);
        }
      }
    }
  }

  protected void fillImporterInfo(Import theImport, String sourceSystemId) {
    if (!xmlImporterMap.containsKey(theImport.getImportType())) {

      if (theImport.getImportType().equals("http://schemas.xmlsoap.org/wsdl/")) {
        Class<?> wsdlImporterClass;
        try {
          wsdlImporterClass = Class.forName("org.activiti.engine.impl.webservice.CxfWSDLImporter", true, Thread.currentThread().getContextClassLoader());
          XMLImporter importerInstance = (XMLImporter) wsdlImporterClass.getDeclaredConstructor().newInstance();
          xmlImporterMap.put(theImport.getImportType(), importerInstance);
          importerInstance.importFrom(theImport, sourceSystemId);

          structureDefinitionMap.putAll(importerInstance.getStructures());
          wsServiceMap.putAll(importerInstance.getServices());
          wsOperationMap.putAll(importerInstance.getOperations());

        } catch (Exception e) {
          throw new ActivitiException("Could not find importer for type " + theImport.getImportType());
        }

      } else {
        throw new ActivitiException("Could not import item of type " + theImport.getImportType());
      }
    }
  }

  protected void returnMessage(List<DataAssociation> dataOutputAssociations, DelegateExecution execution) {
    for (DataAssociation dataAssociationElement : dataOutputAssociations) {
      AbstractDataAssociation dataAssociation = createDataOutputAssociation(dataAssociationElement);
      dataAssociation.evaluate(execution);
    }
  }

  protected void fillMessage(List<DataAssociation> dataInputAssociations, DelegateExecution execution) {
    for (DataAssociation dataAssociationElement : dataInputAssociations) {
      AbstractDataAssociation dataAssociation = createDataInputAssociation(dataAssociationElement);
      dataAssociation.evaluate(execution);
    }
  }

  protected AbstractDataAssociation createDataInputAssociation(DataAssociation dataAssociationElement) {
    if (dataAssociationElement.getAssignments().isEmpty()) {
      return new MessageImplicitDataInputAssociation(dataAssociationElement.getSourceRef(), dataAssociationElement.getTargetRef());
    } else {
      SimpleDataInputAssociation dataAssociation = new SimpleDataInputAssociation(dataAssociationElement.getSourceRef(), dataAssociationElement.getTargetRef());
      ExpressionManager expressionManager = Context.getProcessEngineConfiguration().getExpressionManager();

      for (org.activiti.bpmn.model.Assignment assignmentElement : dataAssociationElement.getAssignments()) {
        if (StringUtils.isNotEmpty(assignmentElement.getFrom()) && StringUtils.isNotEmpty(assignmentElement.getTo())) {
          Expression from = expressionManager.createExpression(assignmentElement.getFrom());
          Expression to = expressionManager.createExpression(assignmentElement.getTo());
          Assignment assignment = new Assignment(from, to);
          dataAssociation.addAssignment(assignment);
        }
      }
      return dataAssociation;
    }
  }

  protected AbstractDataAssociation createDataOutputAssociation(DataAssociation dataAssociationElement) {
    if (StringUtils.isNotEmpty(dataAssociationElement.getSourceRef())) {
      return new MessageImplicitDataOutputAssociation(dataAssociationElement.getTargetRef(), dataAssociationElement.getSourceRef());
    } else {
      ExpressionManager expressionManager = Context.getProcessEngineConfiguration().getExpressionManager();
      Expression transformation = expressionManager.createExpression(dataAssociationElement.getTransformation());
      AbstractDataAssociation dataOutputAssociation = new TransformationDataOutputAssociation(null, dataAssociationElement.getTargetRef(), transformation);
      return dataOutputAssociation;
    }
  }
}
