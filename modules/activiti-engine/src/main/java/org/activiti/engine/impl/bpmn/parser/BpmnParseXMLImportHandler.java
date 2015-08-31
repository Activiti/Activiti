package org.activiti.engine.impl.bpmn.parser;

import org.activiti.engine.impl.bpmn.data.StructureDefinition;
import org.activiti.engine.impl.bpmn.webservice.BpmnInterfaceImplementation;
import org.activiti.engine.impl.bpmn.webservice.OperationImplementation;

public interface BpmnParseXMLImportHandler {
  
  String getSourceSystemId();

  void addStructure(StructureDefinition structure);

  void addService(BpmnInterfaceImplementation bpmnInterfaceImplementation);

  void addOperation(OperationImplementation operationImplementation);
}
