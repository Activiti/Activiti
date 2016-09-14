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
package org.activiti.app.service.editor.mapper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class AbstractInfoMapper implements InfoMapper {

	protected DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	protected ObjectMapper objectMapper = new ObjectMapper();
	protected ArrayNode propertiesNode;

	public ArrayNode map(Object element) {
		propertiesNode = objectMapper.createArrayNode();
		if (element instanceof FlowElement) {
		    FlowElement flowElement = (FlowElement) element;
		    if (StringUtils.isNotEmpty(flowElement.getDocumentation())) {
	            createPropertyNode("Documentation", flowElement.getDocumentation());
	        }
		    
		    if (element instanceof Activity) {
		        Activity activity = (Activity) element;
		        if (activity.getLoopCharacteristics() != null) {
		            MultiInstanceLoopCharacteristics multiInstanceDef = activity.getLoopCharacteristics();
		            createPropertyNode("Multi-instance activity", "");
		            createPropertyNode("Sequential", multiInstanceDef.isSequential());
		            if (StringUtils.isNotEmpty(multiInstanceDef.getInputDataItem())) {
		                createPropertyNode("Collection", multiInstanceDef.getInputDataItem());
		            }
		            if (StringUtils.isNotEmpty(multiInstanceDef.getElementVariable())) {
                        createPropertyNode("Element variable", multiInstanceDef.getElementVariable());
                    }
		            if (StringUtils.isNotEmpty(multiInstanceDef.getLoopCardinality())) {
                        createPropertyNode("Loop cardinality", multiInstanceDef.getLoopCardinality());
                    }
		            if (StringUtils.isNotEmpty(multiInstanceDef.getCompletionCondition())) {
                        createPropertyNode("Completion condition", multiInstanceDef.getCompletionCondition());
                    }
		            createPropertyNode("", "");
                }
    		    if (StringUtils.isNotEmpty(activity.getDefaultFlow())) {
                    createPropertyNode("Default flow", activity.getDefaultFlow());
                }
		    }
		}
		mapProperties(element);
		return propertiesNode;
	}

	protected abstract void mapProperties(Object element);
	
	protected void createListenerPropertyNodes(String name, List<ActivitiListener> listeners) {
	    if (CollectionUtils.isNotEmpty(listeners)) {
            List<String> listenerValues = new ArrayList<String>();
            for (ActivitiListener listener : listeners) {
                StringBuilder listenerBuilder = new StringBuilder();
                listenerBuilder.append(listener.getEvent());
                if (StringUtils.isNotEmpty(listener.getImplementation())) {
                    listenerBuilder.append(" - ");
                    listenerBuilder.append(listener.getImplementation());
                    listenerBuilder.append(" (");
                    listenerBuilder.append(listener.getImplementationType());
                    listenerBuilder.append(")");
                }
                if (CollectionUtils.isNotEmpty(listener.getFieldExtensions())) {
                    listenerBuilder.append(", field extensions: ");
                    for (int i = 0; i < listener.getFieldExtensions().size(); i++) {
                        if (i > 0) {
                            listenerBuilder.append(",  ");
                        }
                        FieldExtension field = listener.getFieldExtensions().get(i);
                        listenerBuilder.append(field.getFieldName());
                        if (StringUtils.isNotEmpty(field.getStringValue())) {
                            listenerBuilder.append(" - ");
                            listenerBuilder.append(field.getStringValue());
                        } else if (StringUtils.isNotEmpty(field.getExpression())) {
                            listenerBuilder.append(" - ");
                            listenerBuilder.append(field.getExpression());
                        }
                    }
                }
                listenerValues.add(listenerBuilder.toString());
            }
            createPropertyNode(name, listenerValues);
        }
	}
	
	protected void createFieldPropertyNodes(String name, List<FieldExtension> fields) {
        if (CollectionUtils.isNotEmpty(fields)) {
            List<String> fieldValues = new ArrayList<String>();
            for (FieldExtension field : fields) {
                StringBuilder fieldBuilder = new StringBuilder();
                fieldBuilder.append(field.getFieldName());
                if (StringUtils.isNotEmpty(field.getStringValue())) {
                    fieldBuilder.append(" - ");
                    fieldBuilder.append(field.getStringValue());
                } else if (StringUtils.isNotEmpty(field.getExpression())) {
                    fieldBuilder.append(" - ");
                    fieldBuilder.append(field.getExpression());
                }
                fieldValues.add(fieldBuilder.toString());
            }
            createPropertyNode(name, fieldValues);
        }
    }

	protected void createPropertyNode(String name, String value) {
		if (StringUtils.isNotEmpty(value)) {
			ObjectNode propertyNode = objectMapper.createObjectNode();
			propertyNode.put("name", name);
			propertyNode.put("value", value);
			propertiesNode.add(propertyNode);
		}
	}

	protected void createPropertyNode(String name, Date value) {
		if (value != null) {
			createPropertyNode(name, dateFormat.format(value));
		}
	}

	protected void createPropertyNode(String name, Boolean value) {
		if (value != null) {
			createPropertyNode(name, value.toString());
		}
	}

	protected void createPropertyNode(String name, List<String> values) {
		if (CollectionUtils.isNotEmpty(values)) {
		    ArrayNode itemsNode = objectMapper.createArrayNode();
			for (String value : values) {
				itemsNode.add(value);
			}
			ObjectNode propertyNode = objectMapper.createObjectNode();
            propertyNode.put("name", name);
            propertyNode.put("type", "list");
            propertyNode.put("value", itemsNode);
			propertiesNode.add(propertyNode);
		}
	}
}
