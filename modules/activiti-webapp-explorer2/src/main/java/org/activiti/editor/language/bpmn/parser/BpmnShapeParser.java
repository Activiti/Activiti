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

import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.activiti.editor.language.bpmn.model.BaseElement;
import org.activiti.editor.language.bpmn.model.Event;
import org.activiti.editor.language.bpmn.model.FlowElement;
import org.activiti.editor.language.bpmn.model.Process;
import org.activiti.editor.language.bpmn.model.SubProcess;

/**
 * @author Tijs Rademakers
 */
public class BpmnShapeParser extends BaseBpmnElementParser {
  
  public static String getElementName() {
    return "BPMNShape";
  }

  public void parse(XMLStreamReader xtr, BpmnModel model, Process activeProcess, 
      List<SubProcess> activeSubProcessList, List<BoundaryEventModel> boundaryList) throws Exception {
    
  	String id = xtr.getAttributeValue(null, "bpmnElement");
		while (xtr.hasNext()) {
			xtr.next();
			if (xtr.isStartElement() && "Bounds".equalsIgnoreCase(xtr.getLocalName())) {
				GraphicInfo graphicInfo = new GraphicInfo();
				graphicInfo.x = Double.valueOf(xtr.getAttributeValue(null, "x")).intValue();
				graphicInfo.y = Double.valueOf(xtr.getAttributeValue(null, "y")).intValue();
				FlowElement flowElement = model.getMainProcess().getFlowElement(id);
				if (flowElement instanceof Event) {
				  graphicInfo.width = 30;
				  graphicInfo.height = 30;
				} else {
				  graphicInfo.width = Double.valueOf(xtr.getAttributeValue(null, "width")).intValue();
				  graphicInfo.height = Double.valueOf(xtr.getAttributeValue(null, "height")).intValue();
				}
				
				model.addGraphicInfo(id, graphicInfo);
				break;
			} else if (xtr.isEndElement() && getElementName().equalsIgnoreCase(xtr.getLocalName())) {
				break;
			}
		}
  }
  
  public BaseElement parseElement() {
  	return null;
  }
}
