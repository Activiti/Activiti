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
import org.activiti.bpmn.converter.alfresco.AlfrescoStartEventXMLConverter;
import org.activiti.bpmn.converter.alfresco.AlfrescoUserTaskXMLConverter;
import org.activiti.bpmn.converter.child.DocumentationParser;
import org.activiti.bpmn.converter.child.ExecutionListenerParser;
import org.activiti.bpmn.converter.child.MultiInstanceParser;
import org.activiti.bpmn.converter.export.BPMNDIExport;
import org.activiti.bpmn.converter.export.DefinitionsRootExport;
import org.activiti.bpmn.converter.export.LaneExport;
import org.activiti.bpmn.converter.export.PoolExport;
import org.activiti.bpmn.converter.export.SignalAndMessageDefinitionExport;
import org.activiti.bpmn.converter.parser.BpmnEdgeParser;
import org.activiti.bpmn.converter.parser.BpmnShapeParser;
import org.activiti.bpmn.converter.parser.LaneParser;
import org.activiti.bpmn.converter.parser.SubProcessParser;
import org.activiti.bpmn.converter.util.ActivitiListenerUtil;
import org.activiti.bpmn.exceptions.XMLException;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.Artifact;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Pool;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.apache.commons.lang.StringUtils;

/**
 * @author Tijs Rademakers
 */
public class BpmnXMLConverter implements BpmnXMLConstants {

  protected static final Logger LOGGER = Logger.getLogger(BpmnXMLConverter.class.getName());
	
	private static Map<String, Class<? extends BaseBpmnXMLConverter>> convertersToBpmnMap = 
	    new HashMap<String, Class<? extends BaseBpmnXMLConverter>>();
	private static Map<Class<? extends BaseElement>, Class<? extends BaseBpmnXMLConverter>> convertersToXMLMap = 
	    new HashMap<Class<? extends BaseElement>, Class<? extends BaseBpmnXMLConverter>>();
	private List<String> userTaskFormTypes;
	private List<String> startEventFormTypes;
	
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
    addConverter(TaskXMLConverter.getXMLType(), TaskXMLConverter.getBpmnElementType(), TaskXMLConverter.class);
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
    
    // artifacts
    addConverter(TextAnnotationXMLConverter.getXMLType(), TextAnnotationXMLConverter.getBpmnElementType(), TextAnnotationXMLConverter.class);
    addConverter(AssociationXMLConverter.getXMLType(), AssociationXMLConverter.getBpmnElementType(), AssociationXMLConverter.class);
    
    // Alfresco types
    addConverter(AlfrescoStartEventXMLConverter.getXMLType(), AlfrescoStartEventXMLConverter.getBpmnElementType(), AlfrescoStartEventXMLConverter.class);
    addConverter(AlfrescoUserTaskXMLConverter.getXMLType(), AlfrescoUserTaskXMLConverter.getBpmnElementType(), AlfrescoUserTaskXMLConverter.class);
  }
  
  private static void addConverter(String elementName, Class<? extends BaseElement> elementClass, 
      Class<? extends BaseBpmnXMLConverter> converter) {
    
    convertersToBpmnMap.put(elementName, converter);
    convertersToXMLMap.put(elementClass, converter);
  }
  
  public void setUserTaskFormTypes(List<String> userTaskFormTypes) {
    this.userTaskFormTypes = userTaskFormTypes;
  }
  
  public void setStartEventFormTypes(List<String> startEventFormTypes) {
    this.startEventFormTypes = startEventFormTypes;
  }

	public BpmnModel convertToBpmnModel(XMLStreamReader xtr) {
	  BpmnModel model = new BpmnModel();
		try {
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

				if (ELEMENT_DEFINITIONS.equalsIgnoreCase(xtr.getLocalName())) {

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
            Process process = new Process();
            process.setId(processId);
            process.setName(xtr.getAttributeValue(null, ATTRIBUTE_NAME));
            model.getProcesses().add(process);
            activeProcess = process;	
				  }
				  
				} else if (ELEMENT_LANE.equalsIgnoreCase(xtr.getLocalName())) {
          new LaneParser().parse(xtr, activeProcess);
					
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

				} else if (ELEMENT_EXECUTION_LISTENER.equalsIgnoreCase(xtr.getLocalName())) {
					
				  new ExecutionListenerParser().parseChildElement(xtr, activeProcess);

				} else {

					if (activeSubProcessList.size() > 0 && ELEMENT_EXTENSIONS.equalsIgnoreCase(xtr.getLocalName())) {
						new ExecutionListenerParser().parseChildElement(xtr, activeSubProcessList.get(activeSubProcessList.size() - 1));

					} else if (activeSubProcessList.size() > 0 && ELEMENT_MULTIINSTANCE.equalsIgnoreCase(xtr.getLocalName())) {
						
						new MultiInstanceParser().parseChildElement(xtr, activeSubProcessList.get(activeSubProcessList.size() - 1));
					  
					} else if (convertersToBpmnMap.containsKey(xtr.getLocalName())) {
					  Class<? extends BaseBpmnXMLConverter> converterClass = convertersToBpmnMap.get(xtr.getLocalName());
					  BaseBpmnXMLConverter converter = converterClass.newInstance();
					  if (userTaskFormTypes != null && ELEMENT_TASK_USER.equals(xtr.getLocalName())) {
					    UserTaskXMLConverter userTaskConverter = (UserTaskXMLConverter) converter;
					    for (String formType : userTaskFormTypes) {
					      userTaskConverter.addFormType(formType);
              }
					  } else if (startEventFormTypes != null && ELEMENT_EVENT_START.equals(xtr.getLocalName())) {
					    StartEventXMLConverter startEventConverter = (StartEventXMLConverter) converter;
              for (String formType : startEventFormTypes) {
                startEventConverter.addFormType(formType);
              }
					  }
					  converter.convertToBpmnModel(xtr, model, activeProcess, activeSubProcessList);
					}
				}
			}

			for (Process process : model.getProcesses()) {
			  processFlowElements(process.getFlowElements(), process);
			}

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error processing BPMN document", e);
		}
		return model;
	}
	
	private void processFlowElements(Collection<FlowElement> flowElementList, BaseElement parentScope) {
	  for (FlowElement flowElement : flowElementList) {
  	  if (flowElement instanceof SequenceFlow) {
        SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
        FlowNode sourceNode = getFlowNodeFromScope(sequenceFlow.getSourceRef(), parentScope);
        if (sourceNode != null) {
          sourceNode.getOutgoingFlows().add(sequenceFlow);
        }
        FlowNode targetNode = getFlowNodeFromScope(sequenceFlow.getTargetRef(), parentScope);
        if (targetNode != null) {
          targetNode.getIncomingFlows().add(sequenceFlow);
        }
      } else if (flowElement instanceof BoundaryEvent) {
        BoundaryEvent boundaryEvent = (BoundaryEvent) flowElement;
        FlowElement attachedToElement = getFlowNodeFromScope(boundaryEvent.getAttachedToRefId(), parentScope);
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
	
	private FlowNode getFlowNodeFromScope(String elementId, BaseElement scope) {
	  FlowNode flowNode = null;
	  if (scope instanceof Process) {
	    flowNode = (FlowNode) ((Process) scope).getFlowElement(elementId);
	  } else if (scope instanceof SubProcess) {
	    flowNode = (FlowNode) ((SubProcess) scope).getFlowElement(elementId);
	  }
	  return flowNode;
	}
	
	public byte[] convertToXML(BpmnModel model) {
    try {

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      
      XMLOutputFactory xof = XMLOutputFactory.newInstance();
      OutputStreamWriter out = new OutputStreamWriter(outputStream, "UTF-8");

      XMLStreamWriter writer = xof.createXMLStreamWriter(out);
      XMLStreamWriter xtw = new IndentingXMLStreamWriter(writer);

      DefinitionsRootExport.writeRootElement(xtw);
      SignalAndMessageDefinitionExport.writeSignalsAndMessages(model, xtw);
      PoolExport.writePools(model, xtw);
      
      for (Process process : model.getProcesses()) {
        
        if(process.getFlowElements().size() == 0 && process.getLanes().size() == 0) {
          // empty process, ignore it 
          continue;
        }
      
        // start process element
        xtw.writeStartElement(ELEMENT_PROCESS);
        xtw.writeAttribute(ATTRIBUTE_ID, process.getId());
        
        if(StringUtils.isNotEmpty(process.getName())) {
          xtw.writeAttribute(ATTRIBUTE_NAME, process.getName());
        }
        
        xtw.writeAttribute(ATTRIBUTE_PROCESS_EXECUTABLE, ATTRIBUTE_VALUE_TRUE);
        
        if (StringUtils.isNotEmpty(process.getDocumentation())) {
  
          xtw.writeStartElement(ELEMENT_DOCUMENTATION);
          xtw.writeCharacters(process.getDocumentation());
          xtw.writeEndElement();
        }
        
        LaneExport.writeLanes(process, xtw);
        
        boolean wroteListener = ActivitiListenerUtil.writeListeners(process, false, xtw);
        if (wroteListener) {
          // closing extensions element
          xtw.writeEndElement();
        }
        
        for (FlowElement flowElement : process.getFlowElements()) {
          createXML(flowElement, xtw);
        }
        
        for (Artifact artifact : process.getArtifacts()) {
          createXML(artifact, xtw);
        }
        
        // end process element
        xtw.writeEndElement();
      }

      BPMNDIExport.writeBPMNDI(model, xtw);

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
      
      if (subProcess instanceof EventSubProcess) {
        xtw.writeAttribute(ATTRIBUTE_TRIGGERED_BY, ATTRIBUTE_VALUE_TRUE);
      }
      
      if (StringUtils.isNotEmpty(subProcess.getDocumentation())) {

        xtw.writeStartElement(ELEMENT_DOCUMENTATION);
        xtw.writeCharacters(subProcess.getDocumentation());
        xtw.writeEndElement();
      }
      
      for (FlowElement subElement : subProcess.getFlowElements()) {
        createXML(subElement, xtw);
      }
      
      for (Artifact artifact : subProcess.getArtifacts()) {
        createXML(artifact, xtw);
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
  
  private void createXML(Artifact artifact, XMLStreamWriter xtw) throws Exception {
    
    Class<? extends BaseBpmnXMLConverter> converter = convertersToXMLMap.get(artifact.getClass());
      
    if (converter == null) {
      throw new XMLException("No converter for " + artifact.getClass() + " found");
    }
      
    converter.newInstance().convertToXML(xtw, artifact);
  }
}
