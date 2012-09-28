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
package org.activiti.editor.language.bpmn.export;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.editor.exception.XMLException;
import org.activiti.editor.json.constants.EditorJsonConstants;
import org.activiti.editor.stencilset.StencilConstants;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class JsonToBpmnExport implements ActivitiNamespaceConstants, EditorJsonConstants, StencilConstants {

  protected static final Logger LOGGER = Logger.getLogger(JsonToBpmnExport.class.getName());
  private IndentingXMLStreamWriter xtw;
  private ObjectNode modelNode;
  private static Map<String, Class<? extends BaseShapeToBpmnExport>> converterMap = new HashMap<String, Class<? extends BaseShapeToBpmnExport>>();
  List<JsonNode> toIgnoreFlows = new ArrayList<JsonNode>();
  
  static {
    converterMap.put(STENCIL_EVENT_START_NONE, StartEventExport.class);
    converterMap.put(STENCIL_EVENT_START_TIMER, StartEventExport.class);
    converterMap.put(STENCIL_EVENT_START_ERROR, StartEventExport.class);
    converterMap.put(STENCIL_EVENT_END_NONE, EndEventExport.class);
    
    converterMap.put(STENCIL_SEQUENCE_FLOW, SequenceFlowExport.class);
    
    converterMap.put(STENCIL_TASK_MANUAL, ManualTaskExport.class);
    converterMap.put(STENCIL_TASK_RECEIVE, ReceiveTaskExport.class);
    converterMap.put(STENCIL_TASK_SCRIPT, ScriptTaskExport.class);
    converterMap.put(STENCIL_TASK_SERVICE, ServiceTaskExport.class);
    converterMap.put(STENCIL_TASK_USER, UserTaskExport.class);
    
    converterMap.put(STENCIL_GATEWAY_EXCLUSIVE, ExclusiveGatewayExport.class);
    converterMap.put(STENCIL_GATEWAY_INCLUSIVE, InclusiveGatewayExport.class);
    converterMap.put(STENCIL_GATEWAY_PARALLEL, ParallelGatewayExport.class);
    converterMap.put(STENCIL_GATEWAY_EVENT, EventGatewayExport.class);
    
    converterMap.put(STENCIL_EVENT_BOUNDARY_TIMER, BoundaryEventExport.class);
    converterMap.put(STENCIL_EVENT_BOUNDARY_ERROR, BoundaryEventExport.class);
    converterMap.put(STENCIL_EVENT_BOUNDARY_SIGNAL, BoundaryEventExport.class);
    
    converterMap.put(STENCIL_EVENT_CATCH_TIMER, CatchEventExport.class);
    converterMap.put(STENCIL_EVENT_CATCH_MESSAGE, CatchEventExport.class);
    converterMap.put(STENCIL_EVENT_CATCH_SIGNAL, CatchEventExport.class);
    
    converterMap.put(STENCIL_EVENT_THROW_NONE, ThrowEventExport.class);
    converterMap.put(STENCIL_EVENT_THROW_SIGNAL, ThrowEventExport.class);
  }
  
  public JsonToBpmnExport(ObjectNode modelNode) {
   this.modelNode = modelNode; 
  }

  public byte[] convert() {
    try {

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	
      XMLOutputFactory xof = XMLOutputFactory.newInstance();
      OutputStreamWriter out = new OutputStreamWriter(outputStream, "UTF-8");

      XMLStreamWriter writer = xof.createXMLStreamWriter(out);
      xtw = new IndentingXMLStreamWriter(writer);

      xtw.writeStartDocument("UTF-8", "1.0");

      // start definitions root element
      xtw.writeStartElement("definitions");
      xtw.setDefaultNamespace(BPMN2_NAMESPACE);
      xtw.writeDefaultNamespace(BPMN2_NAMESPACE);
      xtw.writeNamespace("xsi", XSI_NAMESPACE);
      xtw.writeNamespace(ACTIVITI_EXTENSIONS_PREFIX, ACTIVITI_EXTENSIONS_NAMESPACE);
      xtw.writeNamespace(BPMNDI_PREFIX, BPMNDI_NAMESPACE);
      xtw.writeNamespace(OMGDC_PREFIX, OMGDC_NAMESPACE);
      xtw.writeNamespace(OMGDI_PREFIX, OMGDI_NAMESPACE);
      xtw.writeAttribute("typeLanguage", SCHEMA_NAMESPACE);
      xtw.writeAttribute("expressionLanguage", XPATH_NAMESPACE);
      xtw.writeAttribute("targetNamespace", PROCESS_NAMESPACE);
      
      /*for (Signal signal : model.getSignals()) {
        xtw.writeStartElement("signal");
        xtw.writeAttribute("id", signal.getId());
        xtw.writeAttribute("name", signal.getName());
        xtw.writeEndElement();
      }
      
      if(model.getPools().size() > 0) {
        xtw.writeStartElement("collaboration");
        xtw.writeAttribute("id", "Collaboration");
        for (Pool pool : model.getPools()) {
          xtw.writeStartElement("participant");
          xtw.writeAttribute("id", pool.getId());
          if(StringUtils.isNotEmpty(pool.getName())) {
            xtw.writeAttribute("name", pool.getName());
          }
          xtw.writeAttribute("processRef", pool.getProcessRef());
          xtw.writeEndElement();
        }
        xtw.writeEndElement();
      }*/
      
      // start process element
      xtw.writeStartElement("process");
      xtw.writeAttribute("id", BaseShapeHelper.getPropertyValueAsString(PROPERTY_PROCESS_ID, modelNode));
      
      if(StringUtils.isNotEmpty(BaseShapeHelper.getPropertyValueAsString(PROPERTY_NAME, modelNode))) {
        xtw.writeAttribute("name", BaseShapeHelper.getPropertyValueAsString(PROPERTY_NAME, modelNode));
      }
      
      xtw.writeAttribute("isExecutable", "true");
      
      if (StringUtils.isNotEmpty(BaseShapeHelper.getPropertyValueAsString(PROPERTY_DOCUMENTATION, modelNode))) {

        xtw.writeStartElement("documentation");
        xtw.writeCharacters(BaseShapeHelper.getPropertyValueAsString(PROPERTY_DOCUMENTATION, modelNode));
        xtw.writeEndElement();
      }
      
      ArrayNode shapesArrayNode = (ArrayNode) modelNode.get("childShapes");
      for (JsonNode shapeNode : shapesArrayNode) {
        createXML((ObjectNode) shapeNode);
      }
      
      // end process element
      xtw.writeEndElement();

      new BpmnDIExport().createDIXML(modelNode, xtw);

      // end definitions root element
      xtw.writeEndElement();
      xtw.writeEndDocument();

      xtw.flush();

      outputStream.close();

      xtw.close();
      
      return outputStream.toByteArray();
      
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error writing BPMN XML", e);
      throw new XMLException("Error writing BPMN XML", e);
    }
  }

  private void createXML(ObjectNode objectNode) throws Exception {
    
    String nodeType = getStencilId(objectNode);
    
    if (toIgnoreFlows.contains(objectNode)) {
    	// skip it
    	
    } else if (STENCIL_SUB_PROCESS.equals(nodeType)) {
      
      BaseShapeHelper.writeStartElement("subProcess", objectNode, xtw);
      if (StringUtils.isEmpty(BaseShapeHelper.getPropertyValueAsString("name", objectNode))) {
      	xtw.writeAttribute("name", "subProcess");
      }
      ArrayNode shapesArrayNode = (ArrayNode) objectNode.get("childShapes");
      Map<String, String> flowIds = new HashMap<String, String>();
      for (JsonNode shapeNode : shapesArrayNode) {
        createXML((ObjectNode) shapeNode);
        
        ArrayNode outgoingNode = (ArrayNode) shapeNode.get("outgoing");
        if (outgoingNode != null && outgoingNode.size() > 0) {
          for (JsonNode outgoingChildNode : outgoingNode) {
            JsonNode resourceNode = outgoingChildNode.get(EDITOR_SHAPE_ID);
            if (resourceNode != null) {
            	flowIds.put(resourceNode.asText(), shapeNode.get(EDITOR_SHAPE_ID).asText());
            }
          }
        }
      }
      
      for (String flowId : flowIds.keySet()) {
        for (JsonNode childNode : modelNode.get(EDITOR_CHILD_SHAPES)) {
        	String flowNodeType = getStencilId((ObjectNode) childNode);
          
          if (STENCIL_SEQUENCE_FLOW.equals(flowNodeType) && flowId.equals(childNode.get(EDITOR_SHAPE_ID).asText())) {
          	xtw.writeStartElement("sequenceFlow");
          	xtw.writeAttribute("id", flowId);
          	xtw.writeAttribute("sourceRef", flowIds.get(flowId));
          	xtw.writeAttribute("targetRef", childNode.get("target").get(EDITOR_SHAPE_ID).asText());
          	xtw.writeEndElement();
          	toIgnoreFlows.add(childNode);
          	break;
          }
        }
      }
      
      xtw.writeEndElement();
      
    } else {
    
      Class<? extends BaseShapeToBpmnExport> converter = converterMap.get(nodeType);
      
      if (converter == null) {
        throw new XMLException("Node type " + nodeType + " is unknown");
      }
      
      converter.newInstance().convert(objectNode, xtw, modelNode);
    }
  }
  
  private String getStencilId(ObjectNode objectNode) {
    String stencilId = STENCIL_TASK_SERVICE;
    ObjectNode stencilNode = (ObjectNode) objectNode.get(EDITOR_STENCIL);
    if (stencilNode != null && stencilNode.get(EDITOR_STENCIL_ID) != null && 
        StringUtils.isNotEmpty(stencilNode.get(EDITOR_STENCIL_ID).getTextValue())) {
      
      stencilId = stencilNode.get(EDITOR_STENCIL_ID).getTextValue();
    }
    
    return stencilId;
  }
}
