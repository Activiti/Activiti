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
package org.activiti.editor.language.bpmn.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamReader;

import org.activiti.editor.language.bpmn.model.Activity;
import org.activiti.editor.language.bpmn.model.BaseElement;
import org.activiti.editor.language.bpmn.model.FlowElement;
import org.activiti.editor.language.bpmn.model.Lane;
import org.activiti.editor.language.bpmn.model.Pool;
import org.activiti.editor.language.bpmn.model.Process;
import org.activiti.editor.language.bpmn.model.SequenceFlow;
import org.activiti.editor.language.bpmn.model.Signal;
import org.activiti.editor.language.bpmn.model.SubProcess;
import org.apache.commons.lang.StringUtils;

/**
 * @author Tijs Rademakers
 */
public class BpmnParser {

  protected static final Logger LOGGER = Logger.getLogger(BpmnParser.class.getName());
	
	public List<AssociationModel> associationModels = new ArrayList<AssociationModel>();
	private List<BoundaryEventModel> boundaryList = new ArrayList<BoundaryEventModel>();
	public Map<String, GraphicInfo> labelLocationMap = new HashMap<String, GraphicInfo>();
	
	private static Map<String, Class<? extends BaseBpmnElementParser>> parserMap = new HashMap<String, Class<? extends BaseBpmnElementParser>>();
	
	static {
		// events
	  addParser(EndEventParser.getElementName(), EndEventParser.class);
    addParser(StartEventParser.getElementName(), StartEventParser.class);
    
    // tasks
    addParser(CallActivityParser.getElementName(), CallActivityParser.class);
    addParser(ManualTaskParser.getElementName(), ManualTaskParser.class);
    addParser(ScriptTaskParser.getElementName(), ScriptTaskParser.class);
    addParser(ServiceTaskParser.getElementName(), ServiceTaskParser.class);
    addParser(UserTaskParser.getElementName(), UserTaskParser.class);
    
    // gateways
    addParser(EventGatewayParser.getElementName(), EventGatewayParser.class);
    addParser(ExclusiveGatewayParser.getElementName(), ExclusiveGatewayParser.class);
    addParser(InclusiveGatewayParser.getElementName(), InclusiveGatewayParser.class);
    addParser(ParallelGatewayParser.getElementName(), ParallelGatewayParser.class);
    
    // connectors
    addParser(SequenceFlowParser.getElementName(), SequenceFlowParser.class);
    
    // catch, throw and boundary event
    addParser(CatchEventParser.getElementName(), CatchEventParser.class);
    addParser(ThrowEventParser.getElementName(), ThrowEventParser.class);
    addParser(BoundaryEventParser.getElementName(), BoundaryEventParser.class);
    
    // constructs
    addParser(SubProcessParser.getElementName(), SubProcessParser.class);
    
    // bpmn di
    addParser(BpmnEdgeParser.getElementName(), BpmnEdgeParser.class);
    addParser(BpmnShapeParser.getElementName(), BpmnShapeParser.class);
  }
  
  private static void addParser(String elementName, Class<? extends BaseBpmnElementParser> parser) {
    parserMap.put(elementName, parser);
  }

	public void parseBpmn(XMLStreamReader xtr, BpmnModel model) {
		try {
			boolean processExtensionAvailable = false;
			Process activeProcess = null;
			List<SubProcess> activeSubProcessList = new ArrayList<SubProcess>();
			while (xtr.hasNext()) {
				try {
					xtr.next();
				} catch(Exception e) {
					LOGGER.log(Level.SEVERE, "Error reading XML document", e);
					return;
				}

				if (xtr.isEndElement()  && "subProcess".equalsIgnoreCase(xtr.getLocalName())) {
					activeSubProcessList.remove(activeSubProcessList.size() - 1);
				}

				if (xtr.isStartElement() == false)
					continue;

				if ("definitions".equalsIgnoreCase(xtr.getLocalName())) {

					model.setTargetNamespace(xtr.getAttributeValue(null, "targetNamespace"));
				
				} else if ("signal".equalsIgnoreCase(xtr.getLocalName())) {
					
					if (StringUtils.isNotEmpty(xtr.getAttributeValue(null, "id"))) {
						Signal signal = new Signal();
						signal.setId(xtr.getAttributeValue(null, "id"));
						signal.setName(xtr.getAttributeValue(null, "name"));
						model.getSignals().add(signal);
					}
					
				} else if ("participant".equalsIgnoreCase(xtr.getLocalName())) {
				  
				  if (StringUtils.isNotEmpty(xtr.getAttributeValue(null, "id"))) {
				    Pool pool = new Pool();
				    pool.setId(xtr.getAttributeValue(null, "id"));
				    pool.setName(xtr.getAttributeValue(null, "name"));
				    pool.setProcessRef(xtr.getAttributeValue(null, "processRef"));
				    model.getPools().add(pool);
				  }

				} else if ("process".equalsIgnoreCase(xtr.getLocalName())) {
					
				  if (StringUtils.isNotEmpty(xtr.getAttributeValue(null, "id"))) {
				    String processId = xtr.getAttributeValue(null, "id");
            processExtensionAvailable = true;
            Process process = new Process();
            process.setId(processId);
            process.setName(xtr.getAttributeValue(null, "name"));
            model.getProcesses().add(process);
            activeProcess = process;	
				  }
				  
				} else if ("lane".equalsIgnoreCase(xtr.getLocalName())) {
          Lane lane = new Lane();
          lane.setId(xtr.getAttributeValue(null, "id"));
          lane.setName(xtr.getAttributeValue(null, "name"));
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
          
					
				} else if ("documentation".equalsIgnoreCase(xtr.getLocalName())) {
					
					BaseElement parentElement = null;
					if(activeSubProcessList.size() > 0) {
						parentElement = activeSubProcessList.get(activeSubProcessList.size() - 1);
					} else if(activeProcess != null) {
						parentElement = activeProcess;
					}
					new DocumentationParser().parseChildElement(xtr, parentElement);

				} else if (processExtensionAvailable == true && "executionListener".equalsIgnoreCase(xtr.getLocalName())) {
					
				  new ExecutionListenerParser().parseChildElement(xtr, activeProcess);

				} else {

					processExtensionAvailable = false;

					if (activeSubProcessList.size() > 0 && "extensionElements".equalsIgnoreCase(xtr.getLocalName())) {
						new ExecutionListenerParser().parseChildElement(xtr, activeSubProcessList.get(activeSubProcessList.size() - 1));

					} else if (activeSubProcessList.size() > 0 && "multiInstanceLoopCharacteristics".equalsIgnoreCase(xtr.getLocalName())) {
						
						new MultiInstanceParser().parseChildElement(xtr, activeSubProcessList.get(activeSubProcessList.size() - 1));
						
					} else if (parserMap.containsKey(xtr.getLocalName())) {
					  Class<? extends BaseBpmnElementParser> parser = parserMap.get(xtr.getLocalName());
					  parser.newInstance().parse(xtr, model, activeProcess, activeSubProcessList, boundaryList);
					}
				}
			}

			for (BoundaryEventModel boundaryModel : boundaryList) {
			  FlowElement flowElement = boundaryModel.parentProcess.getFlowElement(boundaryModel.attachedRef);
				if(flowElement != null) {
					boundaryModel.boundaryEvent.setAttachedToRef((Activity) flowElement);
					((Activity) flowElement).getBoundaryEvents().add(boundaryModel.boundaryEvent);
				}
			}
			
			for (FlowElement flowElement : model.getMainProcess().getFlowElements()) {
			  if (flowElement instanceof SequenceFlow == false) continue;
			  
			  SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
			  FlowElement sourceElement = model.getMainProcess().getFlowElement(sequenceFlow.getSourceRef());
			  if (sourceElement != null) {
			    sourceElement.addOutgoingFlow(sequenceFlow);
			  }
			}

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error processing BPMN document", e);
		}
	}
}
