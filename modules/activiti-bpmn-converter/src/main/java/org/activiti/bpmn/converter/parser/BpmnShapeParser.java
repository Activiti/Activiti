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
package org.activiti.bpmn.converter.parser;

import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.GraphicInfo;

/**
 * @author Tijs Rademakers
 */
public class BpmnShapeParser implements BpmnXMLConstants {
  
  public void parse(XMLStreamReader xtr, BpmnModel model) throws Exception {
    
  	String id = xtr.getAttributeValue(null, ATTRIBUTE_DI_BPMNELEMENT);
		while (xtr.hasNext()) {
			xtr.next();
			if (xtr.isStartElement() && ELEMENT_DI_BOUNDS.equalsIgnoreCase(xtr.getLocalName())) {
				GraphicInfo graphicInfo = new GraphicInfo();
				graphicInfo.x = Double.valueOf(xtr.getAttributeValue(null, ATTRIBUTE_DI_X)).intValue();
				graphicInfo.y = Double.valueOf(xtr.getAttributeValue(null, ATTRIBUTE_DI_Y)).intValue();
				FlowElement flowElement = model.getFlowElement(id);
				if (flowElement instanceof Event) {
				  graphicInfo.width = 30;
				  graphicInfo.height = 30;
				} else {
				  graphicInfo.width = Double.valueOf(xtr.getAttributeValue(null, ATTRIBUTE_DI_WIDTH)).intValue();
				  graphicInfo.height = Double.valueOf(xtr.getAttributeValue(null, ATTRIBUTE_DI_HEIGHT)).intValue();
				}
				
				model.addGraphicInfo(id, graphicInfo);
				break;
			} else if (xtr.isEndElement() && ELEMENT_DI_SHAPE.equalsIgnoreCase(xtr.getLocalName())) {
				break;
			}
		}
  }
  
  public BaseElement parseElement() {
  	return null;
  }
}
