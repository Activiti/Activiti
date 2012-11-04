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
package org.activiti.bpmn.converter;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.converter.child.DocumentationParser;
import org.activiti.bpmn.converter.child.ExecutionListenerParser;
import org.activiti.bpmn.converter.child.MultiInstanceParser;
import org.activiti.bpmn.converter.parser.BpmnEdgeParser;
import org.activiti.bpmn.converter.parser.BpmnShapeParser;
import org.activiti.bpmn.converter.parser.SubProcessParser;
import org.activiti.bpmn.exceptions.XMLException;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.AssociationModel;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.Pool;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.SubProcess;
import org.apache.commons.lang.StringUtils;

/**
 * @author Tijs Rademakers
 */
public class BpmnXMLConverter implements BpmnXMLConstants {

  protected static final Logger LOGGER = Logger.getLogger(BpmnXMLConverter.class.getName());
	
	public List<AssociationModel> associationModels = new ArrayList<AssociationModel>();
	public Map<String, GraphicInfo> labelLocationMap = new HashMap<String, GraphicInfo>();
	
	private static Map<String, Class<? extends BaseBpmnXMLConverter>> convertersToBpmnMap = 
	    new HashMap<String, Class<? extends BaseBpmnXMLConverter>>();
	private static Map<Class<? extends BaseElement>, Class<? extends BaseBpmnXMLConverter>> convertersToXMLMap = 
	    new HashMap<Class<? extends BaseElement>, Class<? extends BaseBpmnXMLConverter>>();
	
	static {
		// events
	  addConverter(EndEventXMLConverter.getXMLType(), EndEventXMLConverter.getBpmnElementType(), EndEventXMLConverter.class);
	  addConverter(StartEventXMLConverter.getXMLType(), StartEventXMLConverter.getBpmnElementType(), StartEventXMLConverter.class);
    
    // tasks
	  addConverter(BusinessRuleTaskXMLConverter.getXMLType(), BusinessRuleTaskXMLConverter.getBpmnElementType(), BusinessRuleTaskXMLConverter.class);
    addConverter(ManualTaskXMLConverter.getXMLType(), ManualTaskXMLConverter.getBpmnElementType(), ManualTaskXMLConverter.class);
    addConverter(ReceiveTaskXMLConverter.getXMLType(), ReceiveTaskXMLConverter.getBpmnElementType(), ReceiveTaskXMLConverter.class);
    addConverter(ScriptTaskXMLConverter.getXMLType(), ScriptTaskXMLConverter.getBpmnElementType(), ScriptTaskXMLConverter.class);
    addConverter(ServiceTaskXMLConverter.getXMLType(), ServiceTaskXMLConverter.getBpmnElementType(), ServiceTaskXMLConverter.class);
    addConverter(UserTaskXMLConverter.getXMLType(), UserTaskXMLConverter.getBpmnElementType(), UserTaskXMLConverter.class);
    addConverter(CallActivityXMLConverter.getXMLType(), CallActivityXMLConverter.getBpmnElementType(), CallActivityXMLConverter.class);
    
    // gateways
    addConverter(EventGatewayXMLConverter.getXMLType(), EventGatewayXMLConverter.getBpmnElementType(), EventGatewayXMLConverter.class);
    addConverter(ExclusiveGatewayXMLConverter.getXMLType(), ExclusiveGatewayXMLConverter.getBpmnElementType(), ExclusiveGatewayXMLConverter.class);
    addConverter(InclusiveGatewayXMLConverter.getXMLType(), InclusiveGatewayXMLConverter.getBpmnElementType(), InclusiveGatewayXMLConverter.class);
    addConverter(ParallelGatewayXMLConverter.getXMLType(), ParallelGatewayXMLConverter.getBpmnElementType(), ParallelGatewayXMLConverter.class);
    
    // connectors
    addConverter(SequenceFlowXMLConverter.getXMLType(), SequenceFlowXMLConverter.getBpmnElementType(), SequenceFlowXMLConverter.class);
    
    // catch, throw and boundary event
    addConverter(CatchEventXMLConverter.getXMLType(), CatchEventXMLConverter.getBpmnElementType(), CatchEventXMLConverter.class);
    addConverter(ThrowEventXMLConverter.getXMLType(), ThrowEventXMLConverter.getBpmnElementType(), ThrowEventXMLConverter.class);
    addConverter(BoundaryEventXMLConverter.getXMLType(), BoundaryEventXMLConverter.getBpmnElementType(), BoundaryEventXMLConverter.class);
  }
  
  private static void addConverter(String elementName, Class<? extends BaseElement> elementClass, 
      Class<? extends BaseBpmnXMLConverter> converter) {
    
    convertersToBpmnMap.put(elementName, converter);
    convertersToXMLMap.put(elementClass, converter);
  }

	public BpmnModel convertToBpmnModel(XMLStreamReader xtr) {
	  BpmnModel model = new BpmnModel();
		try {
			boolean processExtensionAvailable = false;
			Process activeProcess = null;
			List<SubProcess> activeSubProcessList = new ArrayList<SubProcess>();
			while (xtr.hasNext()) {
				try {
					xtr.next();
				} catch(Exception e) {
					LOGGER.log(Level.SEVERE, "Error reading XML document", e);
					return model;
				}

				if (xtr.isEndElement()  && ELEMENT_SUBPROCESS.equalsIgnoreCase(xtr.getLocalName())) {
					activeSubProcessList.remove(activeSubProcessList.size() - 1);
				}

				if (xtr.isStartElement() == false)
					continue;

				if ("definitions".equalsIgnoreCase(xtr.getLocalName())) {

					model.setTargetNamespace(xtr.getAttributeValue(null, TARGET_NAMESPACE_ATTRIBUTE));
				
				} else if (ELEMENT_SIGNAL.equalsIgnoreCase(xtr.getLocalName())) {
					
					if (StringUtils.isNotEmpty(xtr.getAttributeValue(null, ATTRIBUTE_ID))) {
						String signalId = xtr.getAttributeValue(null, ATTRIBUTE_ID);
						String signalName = xtr.getAttributeValue(null, ATTRIBUTE_NAME);
						model.addSignal(signalId, signalName);
					}
					
				} else if (ELEMENT_MESSAGE.equalsIgnoreCase(xtr.getLocalName())) {
          
          if (StringUtils.isNotEmpty(xtr.getAttributeValue(null, ATTRIBUTE_ID))) {
            String messageId = xtr.getAttributeValue(null, ATTRIBUTE_ID);
            String messageName = xtr.getAttributeValue(null, ATTRIBUTE_NAME);
            model.addMessage(messageId, messageName);
          }
					
				} else if (ELEMENT_PARTICIPANT.equalsIgnoreCase(xtr.getLocalName())) {
				  
				  if (StringUtils.isNotEmpty(xtr.getAttributeValue(null, ATTRIBUTE_ID))) {
				    Pool pool = new Pool();
				    pool.setId(xtr.getAttributeValue(null, ATTRIBUTE_ID));
				    pool.setName(xtr.getAttributeValue(null, ATTRIBUTE_NAME));
				    pool.setProcessRef(xtr.getAttributeValue(null, ATTRIBUTE_PROCESS_REF));
				    model.getPools().add(pool);
				  }

				} else if (ELEMENT_PROCESS.equalsIgnoreCase(xtr.getLocalName())) {
					
				  if (StringUtils.isNotEmpty(xtr.getAttributeValue(null, ATTRIBUTE_ID))) {
				    String processId = xtr.getAttributeValue(null, ATTRIBUTE_ID);
            processExtensionAvailable = true;
            Process process = new Process();
            process.setId(processId);
            process.setName(xtr.getAttributeValue(null, ATTRIBUTE_NAME));
            model.getProcesses().add(process);
            activeProcess = process;	
				  }
				  
				} else if ("lane".equalsIgnoreCase(xtr.getLocalName())) {
          Lane lane = new Lane();
          lane.setId(xtr.getAttributeValue(null, ATTRIBUTE_ID));
          lane.setName(xtr.getAttributeValue(null, ATTRIBUTE_NAME));
          lane.setParentProcess(activeProcess);
          activeProcess.getLanes().add(lane);
          
          while (xtr.hasNext()) {
            xtr.next();
            if (xtr.isStartElement() && "flowNodeRef".equalsIgnoreCase(xtr.getLocalName())) {
              lane.getFlowReferences().add(xtr.getElementText());
            } else if(xtr.isEndElement() && "lane".equalsIgnoreCase(xtr.getLocalName())) {
              break;
            }
          }
          
					
				} else if (ELEMENT_DOCUMENTATION.equalsIgnoreCase(xtr.getLocalName())) {
					
					BaseElement parentElement = null;
					if(activeSubProcessList.size() > 0) {
						parentElement = activeSubProcessList.get(activeSubProcessList.size() - 1);
					} else if(activeProcess != null) {
						parentElement = activeProcess;
					}
					new DocumentationParser().parseChildElement(xtr, parentElement);
				
				} else if (ELEMENT_SUBPROCESS.equalsIgnoreCase(xtr.getLocalName())) {
          
          new SubProcessParser().parse(xtr, activeSubProcessList, activeProcess);
					
				} else if (ELEMENT_DI_SHAPE.equalsIgnoreCase(xtr.getLocalName())) {
          
          new BpmnShapeParser().parse(xtr, model);
				
				} else if (ELEMENT_DI_EDGE.equalsIgnoreCase(xtr.getLocalName())) {
				  
				  new BpmnEdgeParser().parse(xtr, model);

				} else if (processExtensionAvailable == true && ELEMENT_EXECUTION_LISTENER.equalsIgnoreCase(xtr.getLocalName())) {
					
				  new ExecutionListenerParser().parseChildElement(xtr, activeProcess);

				} else {

					processExtensionAvailable = false;

					if (activeSubProcessList.size() > 0 && ELEMENT_EXTENSIONS.equalsIgnoreCase(xtr.getLocalName())) {
						new ExecutionListenerParser().parseChildElement(xtr, activeSubProcessList.get(activeSubProcessList.size() - 1));

					} else if (activeSubProcessList.size() > 0 && "multiInstanceLoopCharacteristics".equalsIgnoreCase(xtr.getLocalName())) {
						
						new MultiInstanceParser().parseChildElement(xtr, activeSubProcessList.get(activeSubProcessList.size() - 1));
						
					} else if (convertersToBpmnMap.containsKey(xtr.getLocalName())) {
					  Class<? extends BaseBpmnXMLConverter> converter = convertersToBpmnMap.get(xtr.getLocalName());
					  converter.newInstance().convertToBpmnModel(xtr, model, activeProcess, activeSubProcessList);
					}
				}
			}

			processFlowElements(model.getMainProcess().getFlowElements(), model.getMainProcess());

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error processing BPMN document", e);
		}
		return model;
	}
	
	private void processFlowElements(Collection<FlowElement> flowElementList, BaseElement parentScope) {
	  for (FlowElement flowElement : flowElementList) {
  	  if (flowElement instanceof SequenceFlow) {
        SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
        FlowElement sourceElement = getFlowElementFromScope(sequenceFlow.getSourceRef(), parentScope);
        if (sourceElement != null) {
          sourceElement.addOutgoingFlow(sequenceFlow);
        }
      } else if (flowElement instanceof BoundaryEvent) {
        BoundaryEvent boundaryEvent = (BoundaryEvent) flowElement;
        FlowElement attachedToElement = getFlowElementFromScope(boundaryEvent.getAttachedToRefId(), parentScope);
        if(attachedToElement != null) {
          boundaryEvent.setAttachedToRef((Activity) attachedToElement);
          ((Activity) attachedToElement).getBoundaryEvents().add(boundaryEvent);
        }
      } else if(flowElement instanceof SubProcess) {
        SubProcess subProcess = (SubProcess) flowElement;
        processFlowElements(subProcess.getFlowElements(), subProcess);
      }
	  }
	}
	
	private FlowElement getFlowElementFromScope(String elementId, BaseElement scope) {
	  FlowElement flowElement = null;
	  if (scope instanceof Process) {
	    flowElement = ((Process) scope).getFlowElement(elementId);
	  } else if (scope instanceof SubProcess) {
	    flowElement = ((SubProcess) scope).getFlowElement(elementId);
	  }
	  return flowElement;
	}
	
	public byte[] convertToXML(BpmnModel model) {
    try {

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      
      XMLOutputFactory xof = XMLOutputFactory.newInstance();
      OutputStreamWriter out = new OutputStreamWriter(outputStream, "UTF-8");

      XMLStreamWriter writer = xof.createXMLStreamWriter(out);
      XMLStreamWriter xtw = new IndentingXMLStreamWriter(writer);

      xtw.writeStartDocument("UTF-8", "1.0");

      // start definitions root element
      xtw.writeStartElement("definitions");
      xtw.setDefaultNamespace(BPMN2_NAMESPACE);
      xtw.writeDefaultNamespace(BPMN2_NAMESPACE);
      xtw.writeNamespace(XSI_PREFIX, XSI_NAMESPACE);
      xtw.writeNamespace(ACTIVITI_EXTENSIONS_PREFIX, ACTIVITI_EXTENSIONS_NAMESPACE);
      xtw.writeNamespace(BPMNDI_PREFIX, BPMNDI_NAMESPACE);
      xtw.writeNamespace(OMGDC_PREFIX, OMGDC_NAMESPACE);
      xtw.writeNamespace(OMGDI_PREFIX, OMGDI_NAMESPACE);
      xtw.writeAttribute(TYPE_LANGUAGE_ATTRIBUTE, SCHEMA_NAMESPACE);
      xtw.writeAttribute(EXPRESSION_LANGUAGE_ATTRIBUTE, XPATH_NAMESPACE);
      xtw.writeAttribute(TARGET_NAMESPACE_ATTRIBUTE, PROCESS_NAMESPACE);
      
      for (FlowElement flowElement : model.getMainProcess().getFlowElements()) {
        if (flowElement instanceof Event) {
          Event event = (Event) flowElement;
          if (event.getEventDefinitions().size() > 0) {
            EventDefinition eventDefinition = event.getEventDefinitions().get(0);
            if (eventDefinition instanceof SignalEventDefinition) {
              SignalEventDefinition signalEvent = (SignalEventDefinition) eventDefinition;
              if (model.containsSignalId(signalEvent.getSignalRef()) == false) {
                model.addSignal(signalEvent.getSignalRef(), signalEvent.getSignalRef());
              }
              
            } else if (eventDefinition instanceof MessageEventDefinition) {
              MessageEventDefinition messageEvent = (MessageEventDefinition) eventDefinition;
              if (model.containsMessageId(messageEvent.getMessageRef()) == false) {
                model.addMessage(messageEvent.getMessageRef(), messageEvent.getMessageRef());
              }
            }
          }
        }
      }
      
      for (Signal signal : model.getSignals()) {
        xtw.writeStartElement(ELEMENT_SIGNAL);
        xtw.writeAttribute(ATTRIBUTE_ID, signal.getId());
        xtw.writeAttribute(ATTRIBUTE_NAME, signal.getName());
        xtw.writeEndElement();
      }
      
      for (Message message : model.getMessages()) {
        xtw.writeStartElement(ELEMENT_MESSAGE);
        xtw.writeAttribute(ATTRIBUTE_ID, message.getId());
        xtw.writeAttribute(ATTRIBUTE_NAME, message.getName());
        xtw.writeEndElement();
      }
      
      if(model.getPools().size() > 0) {
        xtw.writeStartElement(ELEMENT_COLLABORATION);
        xtw.writeAttribute(ATTRIBUTE_ID, "Collaboration");
        for (Pool pool : model.getPools()) {
          xtw.writeStartElement(ELEMENT_PARTICIPANT);
          xtw.writeAttribute(ATTRIBUTE_ID, pool.getId());
          if(StringUtils.isNotEmpty(pool.getName())) {
            xtw.writeAttribute(ATTRIBUTE_NAME, pool.getName());
          }
          xtw.writeAttribute(ATTRIBUTE_PROCESS_REF, pool.getProcessRef());
          xtw.writeEndElement();
        }
        xtw.writeEndElement();
      }
      
      // start process element
      xtw.writeStartElement(ELEMENT_PROCESS);
      xtw.writeAttribute(ATTRIBUTE_ID, model.getMainProcess().getId());
      
      if(StringUtils.isNotEmpty(model.getMainProcess().getName())) {
        xtw.writeAttribute(ATTRIBUTE_NAME, model.getMainProcess().getName());
      }
      
      xtw.writeAttribute(ATTRIBUTE_PROCESS_EXECUTABLE, ATTRIBUTE_VALUE_TRUE);
      
      if (StringUtils.isNotEmpty(model.getMainProcess().getDocumentation())) {

        xtw.writeStartElement(ELEMENT_DOCUMENTATION);
        xtw.writeCharacters(model.getMainProcess().getDocumentation());
        xtw.writeEndElement();
      }
      
      for (FlowElement flowElement : model.getMainProcess().getFlowElements()) {
        createXML(flowElement, xtw);
      }
      
      // end process element
      xtw.writeEndElement();

      // BPMN DI information
      xtw.writeStartElement(BPMNDI_PREFIX, ELEMENT_DI_DIAGRAM, BPMNDI_NAMESPACE);
      xtw.writeAttribute(ATTRIBUTE_ID, "BPMNDiagram_" + model.getMainProcess().getId());

      xtw.writeStartElement(BPMNDI_PREFIX, ELEMENT_DI_PLANE, BPMNDI_NAMESPACE);
      xtw.writeAttribute(ATTRIBUTE_DI_BPMNELEMENT, model.getMainProcess().getId());
      xtw.writeAttribute(ATTRIBUTE_ID, "BPMNPlane_" + model.getMainProcess().getId());
      
      for (String elementId : model.getLocationMap().keySet()) {
        xtw.writeStartElement(BPMNDI_PREFIX, ELEMENT_DI_SHAPE, BPMNDI_NAMESPACE);
        xtw.writeAttribute(ATTRIBUTE_DI_BPMNELEMENT, elementId);
        xtw.writeAttribute(ATTRIBUTE_ID, "BPMNShape_" + elementId);
        
        GraphicInfo graphicInfo = model.getGraphicInfo(elementId);
        xtw.writeStartElement(OMGDC_PREFIX, ELEMENT_DI_BOUNDS, OMGDC_NAMESPACE);
        xtw.writeAttribute(ATTRIBUTE_DI_HEIGHT, "" + graphicInfo.height);
        xtw.writeAttribute(ATTRIBUTE_DI_WIDTH, "" + graphicInfo.width);
        xtw.writeAttribute(ATTRIBUTE_DI_X, "" + graphicInfo.x);
        xtw.writeAttribute(ATTRIBUTE_DI_Y, "" + graphicInfo.y);
        xtw.writeEndElement();
        
        xtw.writeEndElement();
      }
      
      for (String elementId : model.getFlowLocationMap().keySet()) {
        xtw.writeStartElement(BPMNDI_PREFIX, ELEMENT_DI_EDGE, BPMNDI_NAMESPACE);
        xtw.writeAttribute(ATTRIBUTE_DI_BPMNELEMENT, elementId);
        xtw.writeAttribute(ATTRIBUTE_ID, "BPMNEdge_" + elementId);
        
        List<GraphicInfo> graphicInfoList = model.getFlowLocationGraphicInfo(elementId);
        for (GraphicInfo graphicInfo : graphicInfoList) {
          xtw.writeStartElement(OMGDI_PREFIX, ELEMENT_DI_WAYPOINT, OMGDI_NAMESPACE);
          xtw.writeAttribute(ATTRIBUTE_DI_X, "" + graphicInfo.x);
          xtw.writeAttribute(ATTRIBUTE_DI_Y, "" + graphicInfo.y);
          xtw.writeEndElement();
        }
        
        xtw.writeEndElement();
      }
      
      // end BPMN DI elements
      xtw.writeEndElement();
      xtw.writeEndElement();

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

  private void createXML(FlowElement flowElement, XMLStreamWriter xtw) throws Exception {
    
    if (flowElement instanceof SubProcess) {
      
      SubProcess subProcess = (SubProcess) flowElement;
      xtw.writeStartElement(ELEMENT_SUBPROCESS);
      xtw.writeAttribute(ATTRIBUTE_ID, subProcess.getId());
      if (StringUtils.isNotEmpty(subProcess.getName())) {
        xtw.writeAttribute(ATTRIBUTE_NAME, subProcess.getName());
      } else {
        xtw.writeAttribute(ATTRIBUTE_NAME, "subProcess");
      }
      
      if (StringUtils.isNotEmpty(subProcess.getDocumentation())) {

        xtw.writeStartElement(ELEMENT_DOCUMENTATION);
        xtw.writeCharacters(subProcess.getDocumentation());
        xtw.writeEndElement();
      }
      
      for (FlowElement subElement : subProcess.getFlowElements()) {
        createXML(subElement, xtw);
      }
      
      xtw.writeEndElement();
      
    } else {
    
      Class<? extends BaseBpmnXMLConverter> converter = convertersToXMLMap.get(flowElement.getClass());
      
      if (converter == null) {
        throw new XMLException("No converter for " + flowElement.getClass() + " found");
      }
      
      converter.newInstance().convertToXML(xtw, flowElement);
    }
  }
}
