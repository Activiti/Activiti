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
package org.activiti.editor.language.json.converter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.activiti.editor.json.constants.EditorJsonConstants;
import org.activiti.editor.language.bpmn.model.BaseElement;
import org.activiti.editor.language.bpmn.model.BoundaryEvent;
import org.activiti.editor.language.bpmn.model.EndEvent;
import org.activiti.editor.language.bpmn.model.EventGateway;
import org.activiti.editor.language.bpmn.model.ExclusiveGateway;
import org.activiti.editor.language.bpmn.model.FlowElement;
import org.activiti.editor.language.bpmn.model.InclusiveGateway;
import org.activiti.editor.language.bpmn.model.IntermediateCatchEvent;
import org.activiti.editor.language.bpmn.model.ManualTask;
import org.activiti.editor.language.bpmn.model.ParallelGateway;
import org.activiti.editor.language.bpmn.model.Process;
import org.activiti.editor.language.bpmn.model.ScriptTask;
import org.activiti.editor.language.bpmn.model.SequenceFlow;
import org.activiti.editor.language.bpmn.model.ServiceTask;
import org.activiti.editor.language.bpmn.model.StartEvent;
import org.activiti.editor.language.bpmn.model.SubProcess;
import org.activiti.editor.language.bpmn.model.ThrowEvent;
import org.activiti.editor.language.bpmn.model.UserTask;
import org.activiti.editor.language.bpmn.parser.BpmnModel;
import org.activiti.editor.language.bpmn.parser.BpmnParser;
import org.activiti.editor.stencilset.StencilConstants;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class BpmnToEditorJsonConverter implements EditorJsonConstants, StencilConstants, ActivityProcessor {
  
  protected static final Logger LOGGER = Logger.getLogger(BpmnToEditorJsonConverter.class.getName());
  private ObjectMapper objectMapper = new ObjectMapper();
  private String resourceName;
  private InputStream inputStream;
  
  private static Map<Class<? extends BaseElement>, BaseBpmnElementToJsonConverter> converterMap = 
      new HashMap<Class<? extends BaseElement>, BaseBpmnElementToJsonConverter>();
  
  static {
    
    // start and end events
    converterMap.put(StartEvent.class, new StartEventConverter());
    converterMap.put(EndEvent.class, new EndEventConverter());
    
    // connectors
    converterMap.put(SequenceFlow.class, new SequenceFlowConverter());
    
    // task types
    converterMap.put(ManualTask.class, new ManualTaskConverter());
    converterMap.put(ScriptTask.class, new ScriptTaskConverter());
    converterMap.put(ServiceTask.class, new ServiceTaskConverter());
    converterMap.put(UserTask.class, new UserTaskConverter());
    
    // gateways
    converterMap.put(ExclusiveGateway.class, new ExclusiveGatewayConverter());
    converterMap.put(InclusiveGateway.class, new InclusiveGatewayConverter());
    converterMap.put(ParallelGateway.class, new ParallelGatewayConverter());
    converterMap.put(EventGateway.class, new EventGatewayConverter());
    
    // scope constructs
    converterMap.put(SubProcess.class, new SubProcessConverter());
    
    // catch events
    converterMap.put(IntermediateCatchEvent.class, new CatchEventConverter());
    
    // throw events
    converterMap.put(ThrowEvent.class, new ThrowEventConverter());
    
    // boundary events
    converterMap.put(BoundaryEvent.class, new BoundaryEventConverter());
  }
  
  public BpmnToEditorJsonConverter(String resourceName, InputStream inputStream) {
    this.resourceName = resourceName;
    this.inputStream = inputStream;
  }

  public ObjectNode convertToJson() {
    ObjectNode modelNode = objectMapper.createObjectNode();
    modelNode.put("bounds", BpmnJsonConverterUtil.createBoundsNode(1485, 1050, 0, 0));
    modelNode.put("resourceId", "canvas");
    
    ObjectNode stencilNode = objectMapper.createObjectNode();
    stencilNode.put("id", "BPMNDiagram");
    modelNode.put("stencil", stencilNode);
    
    ObjectNode stencilsetNode = objectMapper.createObjectNode();
    stencilsetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
    stencilsetNode.put("url", "../editor/stencilsets/bpmn2.0/bpmn2.0.json");
    modelNode.put("stencilset", stencilsetNode);
    
    ArrayNode shapesArrayNode = objectMapper.createArrayNode();
    
    BpmnModel model = new BpmnModel();
    try {
      XMLInputFactory xif = XMLInputFactory.newInstance();
      InputStreamReader in = new InputStreamReader(inputStream, "UTF-8");
      XMLStreamReader xtr = xif.createXMLStreamReader(in);
      new BpmnParser().parseBpmn(xtr, model);
    } catch(Exception e) {
      LOGGER.log(Level.SEVERE, "Error parsing BPMN XML", e);
      return modelNode;
    }
    
    Process process = model.getMainProcess();
      
    ObjectNode propertiesNode = objectMapper.createObjectNode();
    if (StringUtils.isNotEmpty(process.getId())) {
      propertiesNode.put(PROPERTY_PROCESS_ID, process.getId());
    }
    if (StringUtils.isNotEmpty(process.getName())) {
      propertiesNode.put(PROPERTY_NAME, process.getName());
    }
    if (StringUtils.isNotEmpty(process.getDocumentation())) {
      propertiesNode.put(PROPERTY_DOCUMENTATION, process.getDocumentation());
    }
    modelNode.put(EDITOR_SHAPE_PROPERTIES, propertiesNode);
    processFlowElements(process, model, shapesArrayNode, 0.0, 0.0);
    
    modelNode.put(EDITOR_CHILD_SHAPES, shapesArrayNode);
    return modelNode;
  }
  
  public void processFlowElements(Process process, BpmnModel model, ArrayNode shapesArrayNode, 
      double subProcessX, double subProcessY) {
    
    for (FlowElement flowElement : process.getFlowElements()) {
      BaseBpmnElementToJsonConverter converter = converterMap.get(flowElement.getClass());
      if (converter != null) {
        converter.convert(flowElement, this, process, model, shapesArrayNode, 
            subProcessX, subProcessY);
      }
    }
  }
}
