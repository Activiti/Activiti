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

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BooleanDataObject;
import org.activiti.bpmn.model.DateDataObject;
import org.activiti.bpmn.model.DoubleDataObject;
import org.activiti.bpmn.model.EventListener;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.IntegerDataObject;
import org.activiti.bpmn.model.ItemDefinition;
import org.activiti.bpmn.model.LongDataObject;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StringDataObject;
import org.activiti.bpmn.model.UserTask;
import org.activiti.bpmn.model.ValuedDataObject;
import org.activiti.editor.constants.EditorJsonConstants;
import org.activiti.editor.constants.StencilConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class BpmnJsonConverterUtil implements EditorJsonConstants, StencilConstants {
  
  private static final Logger logger = LoggerFactory.getLogger(BpmnJsonConverterUtil.class);
  
  private static DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeParser();
  private static ObjectMapper objectMapper = new ObjectMapper();

  public static ObjectNode createChildShape(String id, String type, double lowerRightX, double lowerRightY, double upperLeftX, double upperLeftY) {
    ObjectNode shapeNode = objectMapper.createObjectNode();
    shapeNode.put(EDITOR_BOUNDS, createBoundsNode(lowerRightX, lowerRightY, upperLeftX, upperLeftY));
    shapeNode.put(EDITOR_SHAPE_ID, id);
    ArrayNode shapesArrayNode = objectMapper.createArrayNode();
    shapeNode.put(EDITOR_CHILD_SHAPES, shapesArrayNode);
    ObjectNode stencilNode = objectMapper.createObjectNode();
    stencilNode.put(EDITOR_STENCIL_ID, type);
    shapeNode.put(EDITOR_STENCIL, stencilNode);
    return shapeNode;
  }
  
  public static ObjectNode createBoundsNode(double lowerRightX, double lowerRightY, double upperLeftX, double upperLeftY) {
    ObjectNode boundsNode = objectMapper.createObjectNode();
    boundsNode.put(EDITOR_BOUNDS_LOWER_RIGHT, createPositionNode(lowerRightX, lowerRightY));
    boundsNode.put(EDITOR_BOUNDS_UPPER_LEFT, createPositionNode(upperLeftX, upperLeftY));
    return boundsNode;
  }
  
  public static ObjectNode createPositionNode(double x, double y) {
    ObjectNode positionNode = objectMapper.createObjectNode();
    positionNode.put(EDITOR_BOUNDS_X, x);
    positionNode.put(EDITOR_BOUNDS_Y, y);
    return positionNode;
  }
  
  public static ObjectNode createResourceNode(String id) {
    ObjectNode resourceNode = objectMapper.createObjectNode();
    resourceNode.put(EDITOR_SHAPE_ID, id);
    return resourceNode;
  }
  
  public static String getStencilId(JsonNode objectNode) {
    String stencilId = null;
    JsonNode stencilNode = objectNode.get(EDITOR_STENCIL);
    if (stencilNode != null && stencilNode.get(EDITOR_STENCIL_ID) != null) {
      stencilId = stencilNode.get(EDITOR_STENCIL_ID).asText();
    }
    return stencilId;
  }
  
  public static String getElementId(JsonNode objectNode) {
    String elementId = null;
    if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_OVERRIDE_ID, objectNode))) {
      elementId = getPropertyValueAsString(PROPERTY_OVERRIDE_ID, objectNode).trim();
    } else {
      elementId = objectNode.get(EDITOR_SHAPE_ID).asText();
    }
    
    return elementId;
  }
  
  public static void convertListenersToJson(List<ActivitiListener> listeners, boolean isExecutionListener, ObjectNode propertiesNode) {
      String propertyName = null;
      String valueName = null;
      if (isExecutionListener) {
          propertyName = PROPERTY_EXECUTION_LISTENERS;
          valueName = "executionListeners";

      } else {
          propertyName = PROPERTY_TASK_LISTENERS;
          valueName = "taskListeners";
      }

      ObjectNode listenersNode = objectMapper.createObjectNode();
      ArrayNode itemsNode = objectMapper.createArrayNode();
      for (ActivitiListener listener : listeners) {
          ObjectNode propertyItemNode = objectMapper.createObjectNode();

          propertyItemNode.put(PROPERTY_LISTENER_EVENT, listener.getEvent());

          if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(listener.getImplementationType())) {
              propertyItemNode.put(PROPERTY_LISTENER_CLASS_NAME, listener.getImplementation());
          } else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equals(listener.getImplementationType())) {
              propertyItemNode.put(PROPERTY_LISTENER_EXPRESSION, listener.getImplementation());
          } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(listener.getImplementationType())) {
              propertyItemNode.put(PROPERTY_LISTENER_DELEGATE_EXPRESSION, listener.getImplementation());
          }

          if (CollectionUtils.isNotEmpty(listener.getFieldExtensions())) {
              ArrayNode fieldsArray = objectMapper.createArrayNode();
              for (FieldExtension fieldExtension : listener.getFieldExtensions()) {
                  ObjectNode fieldNode = objectMapper.createObjectNode();
                  fieldNode.put(PROPERTY_FIELD_NAME, fieldExtension.getFieldName());
                  if (StringUtils.isNotEmpty(fieldExtension.getStringValue())) {
                      fieldNode.put(PROPERTY_FIELD_STRING_VALUE, fieldExtension.getStringValue());
                  }
                  if (StringUtils.isNotEmpty(fieldExtension.getExpression())) {
                      fieldNode.put(PROPERTY_FIELD_EXPRESSION, fieldExtension.getExpression());
                  }
                  fieldsArray.add(fieldNode);
              }
              propertyItemNode.put(PROPERTY_LISTENER_FIELDS, fieldsArray);
          }

          itemsNode.add(propertyItemNode);
      }

      listenersNode.put(valueName, itemsNode);
      propertiesNode.put(propertyName, listenersNode);
  }
  
  public static void convertEventListenersToJson(List<EventListener> listeners, ObjectNode propertiesNode) {
      ObjectNode listenersNode = objectMapper.createObjectNode();
      ArrayNode itemsNode = objectMapper.createArrayNode();
      for (EventListener listener : listeners) {
          ObjectNode propertyItemNode = objectMapper.createObjectNode();

          if (StringUtils.isNotEmpty(listener.getEvents())) {
              ArrayNode eventArrayNode = objectMapper.createArrayNode();
              String[] eventArray = listener.getEvents().split(",");
              for (String eventValue : eventArray) {
                  if (StringUtils.isNotEmpty(eventValue.trim())) {
                      ObjectNode eventNode = objectMapper.createObjectNode();
                      eventNode.put(PROPERTY_EVENTLISTENER_EVENT, eventValue.trim());
                      eventArrayNode.add(eventNode);
                  }
              }
              propertyItemNode.put(PROPERTY_EVENTLISTENER_EVENT, listener.getEvents());
              propertyItemNode.put(PROPERTY_EVENTLISTENER_EVENTS, eventArrayNode);
          }

          String implementationText = null;
          if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(listener.getImplementationType())) {
              propertyItemNode.put(PROPERTY_EVENTLISTENER_CLASS_NAME, listener.getImplementation());
              implementationText = listener.getImplementation();
          
          } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(listener.getImplementationType())) {
              propertyItemNode.put(PROPERTY_EVENTLISTENER_DELEGATE_EXPRESSION, listener.getImplementation());
              implementationText = listener.getImplementation();
          
          } else if (ImplementationType.IMPLEMENTATION_TYPE_THROW_ERROR_EVENT.equals(listener.getImplementationType())) {
              propertyItemNode.put(PROPERTY_EVENTLISTENER_RETHROW_EVENT, true);
              propertyItemNode.put(PROPERTY_EVENTLISTENER_RETHROW_TYPE, "error");
              propertyItemNode.put(PROPERTY_EVENTLISTENER_ERROR_CODE, listener.getImplementation());
              implementationText = "Rethrow as error " + listener.getImplementation();
              
          } else if (ImplementationType.IMPLEMENTATION_TYPE_THROW_MESSAGE_EVENT.equals(listener.getImplementationType())) {
              propertyItemNode.put(PROPERTY_EVENTLISTENER_RETHROW_EVENT, true);
              propertyItemNode.put(PROPERTY_EVENTLISTENER_RETHROW_TYPE, "message");
              propertyItemNode.put(PROPERTY_EVENTLISTENER_MESSAGE_NAME, listener.getImplementation());
              implementationText = "Rethrow as message " + listener.getImplementation();
          
          } else if (ImplementationType.IMPLEMENTATION_TYPE_THROW_SIGNAL_EVENT.equals(listener.getImplementationType())) {
              propertyItemNode.put(PROPERTY_EVENTLISTENER_RETHROW_EVENT, true);
              propertyItemNode.put(PROPERTY_EVENTLISTENER_RETHROW_TYPE, "signal");
              propertyItemNode.put(PROPERTY_EVENTLISTENER_SIGNAL_NAME, listener.getImplementation());
              implementationText = "Rethrow as signal " + listener.getImplementation();
          
          } else if (ImplementationType.IMPLEMENTATION_TYPE_THROW_GLOBAL_SIGNAL_EVENT.equals(listener.getImplementationType())) {
              propertyItemNode.put(PROPERTY_EVENTLISTENER_RETHROW_EVENT, true);
              propertyItemNode.put(PROPERTY_EVENTLISTENER_RETHROW_TYPE, "globalSignal");
              propertyItemNode.put(PROPERTY_EVENTLISTENER_SIGNAL_NAME, listener.getImplementation());
              implementationText = "Rethrow as signal " + listener.getImplementation();
          }
          
          if (StringUtils.isNotEmpty(implementationText)) {
              propertyItemNode.put(PROPERTY_EVENTLISTENER_IMPLEMENTATION, implementationText);
          }

          if (StringUtils.isNotEmpty(listener.getEntityType())) {
              propertyItemNode.put(PROPERTY_EVENTLISTENER_ENTITY_TYPE, listener.getEntityType());
          }

          itemsNode.add(propertyItemNode);
      }

      listenersNode.put(PROPERTY_EVENTLISTENER_VALUE, itemsNode);
      propertiesNode.put(PROPERTY_EVENT_LISTENERS, listenersNode);
  }
  
  public static void convertJsonToListeners(JsonNode objectNode, BaseElement element) {
    JsonNode executionListenersNode = getProperty(PROPERTY_EXECUTION_LISTENERS, objectNode);
    if (executionListenersNode != null) {
      executionListenersNode = validateIfNodeIsTextual(executionListenersNode);
      JsonNode listenersNode = executionListenersNode.get("executionListeners");
      parseListeners(listenersNode, element, false);
    }
    
    if (element instanceof UserTask) {
      JsonNode taskListenersNode = getProperty(PROPERTY_TASK_LISTENERS, objectNode);
      if (taskListenersNode != null) {
        taskListenersNode = validateIfNodeIsTextual(taskListenersNode);
        JsonNode listenersNode = taskListenersNode.get("taskListeners");
        parseListeners(listenersNode, element, true);
      }
    }
  }
  
  protected static void parseListeners(JsonNode listenersNode, BaseElement element, boolean isTaskListener) {  
    if (listenersNode == null) return;
    
    for (JsonNode listenerNode : listenersNode) {
      JsonNode eventNode = listenerNode.get(PROPERTY_LISTENER_EVENT);
      if (eventNode != null && eventNode.isNull() == false && StringUtils.isNotEmpty(eventNode.asText())) {
        
        ActivitiListener listener = new ActivitiListener();
        listener.setEvent(eventNode.asText());
        if (StringUtils.isNotEmpty(getValueAsString(PROPERTY_LISTENER_CLASS_NAME, listenerNode))) {
          listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
          listener.setImplementation(getValueAsString(PROPERTY_LISTENER_CLASS_NAME, listenerNode));
        } else if (StringUtils.isNotEmpty(getValueAsString(PROPERTY_LISTENER_EXPRESSION, listenerNode))) {
          listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION);
          listener.setImplementation(getValueAsString(PROPERTY_LISTENER_EXPRESSION, listenerNode));
        } else if (StringUtils.isNotEmpty(getValueAsString(PROPERTY_LISTENER_DELEGATE_EXPRESSION, listenerNode))) {
          listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
          listener.setImplementation(getValueAsString(PROPERTY_LISTENER_DELEGATE_EXPRESSION, listenerNode));
        }
        
        JsonNode fieldsNode = listenerNode.get(PROPERTY_LISTENER_FIELDS);
        if (fieldsNode != null) {
          for (JsonNode fieldNode : fieldsNode) {
            JsonNode nameNode = fieldNode.get(PROPERTY_FIELD_NAME);
            if (nameNode != null && nameNode.isNull() == false && StringUtils.isNotEmpty(nameNode.asText())) {
              FieldExtension fieldExtension = new FieldExtension();
              fieldExtension.setFieldName(nameNode.asText());
              fieldExtension.setStringValue(getValueAsString(PROPERTY_FIELD_STRING_VALUE, fieldNode));
              if (StringUtils.isEmpty(fieldExtension.getStringValue())) {
                fieldExtension.setStringValue(getValueAsString(PROPERTY_FIELD_STRING, fieldNode));
              }
              if (StringUtils.isEmpty(fieldExtension.getStringValue())) {
                fieldExtension.setExpression(getValueAsString(PROPERTY_FIELD_EXPRESSION, fieldNode));
              }
              listener.getFieldExtensions().add(fieldExtension);
            }
          }
        }
        
        if (element instanceof Process) {
          ((Process) element).getExecutionListeners().add(listener);
        } else if (element instanceof SequenceFlow) {
          ((SequenceFlow) element).getExecutionListeners().add(listener);
        } else if (element instanceof UserTask) {
          if (isTaskListener) {
            ((UserTask) element).getTaskListeners().add(listener);
          } else {
            ((UserTask) element).getExecutionListeners().add(listener);
          }
        } else if (element instanceof Activity) {
          ((Activity) element).getExecutionListeners().add(listener);
        }
      }
    }
  }
  
  public static void parseEventListeners(JsonNode listenersNode, Process process) {  
      if (listenersNode == null) return;
      listenersNode = validateIfNodeIsTextual(listenersNode);
      for (JsonNode listenerNode : listenersNode) {
        JsonNode eventsNode = listenerNode.get(PROPERTY_EVENTLISTENER_EVENTS);
        if (eventsNode != null && eventsNode.isArray() && eventsNode.size() > 0) {
          
          EventListener listener = new EventListener();
          StringBuilder eventsBuilder = new StringBuilder();
          for (JsonNode eventNode : eventsNode) {
              JsonNode eventValueNode = eventNode.get(PROPERTY_EVENTLISTENER_EVENT);
              if (eventValueNode != null && eventValueNode.isNull() == false && StringUtils.isNotEmpty(eventValueNode.asText())) {
                  if (eventsBuilder.length() > 0) {
                      eventsBuilder.append(",");
                  }
                  eventsBuilder.append(eventValueNode.asText());
              }
          }
          
          if (eventsBuilder.length() == 0) continue;
          
          listener.setEvents(eventsBuilder.toString());
          
          JsonNode rethrowEventNode = listenerNode.get("rethrowEvent");
          if (rethrowEventNode != null && rethrowEventNode.asBoolean()) {
              JsonNode rethrowTypeNode = listenerNode.get("rethrowType");
              if (rethrowTypeNode != null) {
                  if ("error".equalsIgnoreCase(rethrowTypeNode.asText())) {
                      String errorCode = getValueAsString("errorcode", listenerNode);
                      if (StringUtils.isNotEmpty(errorCode)) {
                          listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_THROW_ERROR_EVENT);
                          listener.setImplementation(errorCode);
                      }
                  
                  } else if ("message".equalsIgnoreCase(rethrowTypeNode.asText())) {
                      String messageName = getValueAsString("messagename", listenerNode);
                      if (StringUtils.isNotEmpty(messageName)) {
                          listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_THROW_MESSAGE_EVENT);
                          listener.setImplementation(messageName);
                      }
                  
                  } else if ("signal".equalsIgnoreCase(rethrowTypeNode.asText())) {
                      String signalName = getValueAsString("signalname", listenerNode);
                      if (StringUtils.isNotEmpty(signalName)) {
                          listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_THROW_SIGNAL_EVENT);
                          listener.setImplementation(signalName);
                      }
                  
                  } else if ("globalSignal".equalsIgnoreCase(rethrowTypeNode.asText())) {
                      String signalName = getValueAsString("signalname", listenerNode);
                      if (StringUtils.isNotEmpty(signalName)) {
                          listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_THROW_GLOBAL_SIGNAL_EVENT);
                          listener.setImplementation(signalName);
                      }
                  }
              }
              
              if (StringUtils.isEmpty(listener.getImplementation())) {
                  continue;
              }
          
          } else {
          
              if (StringUtils.isNotEmpty(getValueAsString(PROPERTY_EVENTLISTENER_CLASS_NAME, listenerNode))) {
                  listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
                  listener.setImplementation(getValueAsString(PROPERTY_EVENTLISTENER_CLASS_NAME, listenerNode));
                
              } else if (StringUtils.isNotEmpty(getValueAsString(PROPERTY_EVENTLISTENER_DELEGATE_EXPRESSION, listenerNode))) {
                  listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
                  listener.setImplementation(getValueAsString(PROPERTY_EVENTLISTENER_DELEGATE_EXPRESSION, listenerNode));
              }
              
              if (StringUtils.isNotEmpty(getValueAsString(PROPERTY_EVENTLISTENER_ENTITY_TYPE, listenerNode))) {
                  listener.setEntityType(getValueAsString(PROPERTY_EVENTLISTENER_ENTITY_TYPE, listenerNode));
              }
              
              if (StringUtils.isEmpty(listener.getImplementation())) {
                  continue;
              }
          }
 
          process.getEventListeners().add(listener);
        }
      }
    }
  
  public static String lookForSourceRef(String flowId, JsonNode childShapesNode) {
    String sourceRef = null;
    
    if (childShapesNode != null) {
    
      for (JsonNode childNode : childShapesNode) {
        JsonNode outgoingNode = childNode.get("outgoing");
        if (outgoingNode != null && outgoingNode.size() > 0) {
          for (JsonNode outgoingChildNode : outgoingNode) {
            JsonNode resourceNode = outgoingChildNode.get(EDITOR_SHAPE_ID);
            if (resourceNode != null && flowId.equals(resourceNode.asText())) {
              sourceRef = BpmnJsonConverterUtil.getElementId(childNode);
              break;
            }
          }
          
          if (sourceRef != null) {
            break;
          }
        }
        sourceRef = lookForSourceRef(flowId, childNode.get(EDITOR_CHILD_SHAPES));
        
        if (sourceRef != null) {
          break;
        }
      }
    }
    return sourceRef;
  }
  
  public static  List<ValuedDataObject> convertJsonToDataProperties(JsonNode objectNode, BaseElement element) {
    List<ValuedDataObject> dataObjects = new ArrayList<ValuedDataObject>();

    if (objectNode != null) {
      if (objectNode.isValueNode() && StringUtils.isNotEmpty(objectNode.asText())) {
        try {
          objectNode = objectMapper.readTree(objectNode.asText());
        } catch (Exception e) {
          logger.info("Data properties node cannot be read", e);
        }
      }

      JsonNode itemsArrayNode = objectNode.get(EDITOR_PROPERTIES_GENERAL_ITEMS);
      if (itemsArrayNode != null) {
        for (JsonNode dataNode : itemsArrayNode) {

          JsonNode dataIdNode = dataNode.get(PROPERTY_DATA_ID);
          if (dataIdNode != null && StringUtils.isNotEmpty(dataIdNode.asText())) {
            ValuedDataObject dataObject = null;
            ItemDefinition itemSubjectRef = new ItemDefinition();
            String dataType = dataNode.get(PROPERTY_DATA_TYPE).asText();

            if (dataType.equals("string")) {
              dataObject = new StringDataObject();
            } else if (dataType.equals("int")) {
              dataObject = new IntegerDataObject();
            } else if (dataType.equals("long")) {
              dataObject = new LongDataObject();
            } else if (dataType.equals("double")) {
              dataObject = new DoubleDataObject();
            } else if (dataType.equals("boolean")) {
              dataObject = new BooleanDataObject();
            } else if (dataType.equals("datetime")) {
              dataObject = new DateDataObject();
            } else {
              logger.error("Error converting {}", dataIdNode.asText());
            }

            if (null != dataObject) {
              dataObject.setId(dataIdNode.asText());
              dataObject.setName(dataNode.get(PROPERTY_DATA_NAME).asText());

              itemSubjectRef.setStructureRef("xsd:" + dataType);
              dataObject.setItemSubjectRef(itemSubjectRef);

              if (dataObject instanceof DateDataObject) {
                try {
                  dataObject.setValue(dateTimeFormatter.parseDateTime(dataNode.get(PROPERTY_DATA_VALUE).asText()).toDate());
                } catch (Exception e) {
                  logger.error("Error converting {}", dataObject.getName(), e);
                }
              } else {
                dataObject.setValue(dataNode.get(PROPERTY_DATA_VALUE).asText());
              }

              dataObjects.add(dataObject);
            }
          }
        }
      }
    }
    return dataObjects;
  }

  public static void convertDataPropertiesToJson(List<ValuedDataObject> dataObjects, ObjectNode propertiesNode) {
    ObjectNode dataPropertiesNode = objectMapper.createObjectNode();
    ArrayNode itemsNode = objectMapper.createArrayNode();

    for (ValuedDataObject dObj : dataObjects) {
      ObjectNode propertyItemNode = objectMapper.createObjectNode();
      propertyItemNode.put(PROPERTY_DATA_ID, dObj.getId());
      propertyItemNode.put(PROPERTY_DATA_NAME, dObj.getName());

      String itemSubjectRefQName = dObj.getItemSubjectRef().getStructureRef();
      // remove namespace prefix
      String dataType = itemSubjectRefQName.substring(itemSubjectRefQName.indexOf(':') + 1);
      propertyItemNode.put(PROPERTY_DATA_TYPE, dataType);

      Object dObjValue = dObj.getValue();
      String value = new String();
      if (null == dObjValue) {
        propertyItemNode.put(PROPERTY_DATA_VALUE, "");
      } else {
        if ("datetime".equals(dataType)) {
          value = new DateTime(dObjValue).toString("yyyy-MM-dd'T'hh:mm:ss");
        } else {
          value = new String(dObjValue.toString());
        }
        propertyItemNode.put(PROPERTY_DATA_VALUE, value.toString());
      }

      itemsNode.add(propertyItemNode);
    }

    dataPropertiesNode.put(EDITOR_PROPERTIES_GENERAL_ITEMS, itemsNode);
    propertiesNode.put("dataproperties", dataPropertiesNode);
  }
  
  public static JsonNode validateIfNodeIsTextual(JsonNode node) {
    if (node != null && node.isNull() == false && node.isTextual() && StringUtils.isNotEmpty(node.asText())) {
      try {
        node = objectMapper.readTree(node.asText());
      } catch(Exception e) {
        logger.error("Error converting textual node", e);
      }
    }
    return node;
  }
  
  public static String getValueAsString(String name, JsonNode objectNode) {
    String propertyValue = null;
    JsonNode propertyNode = objectNode.get(name);
    if (propertyNode != null && propertyNode.isNull() == false) {
      propertyValue = propertyNode.asText();
    }
    return propertyValue;
  }
  
  public static String getPropertyValueAsString(String name, JsonNode objectNode) {
    String propertyValue = null;
    JsonNode propertyNode = getProperty(name, objectNode);
    if (propertyNode != null && propertyNode.isNull() == false) {
      propertyValue = propertyNode.asText();
    }
    return propertyValue;
  }
  
  public static JsonNode getProperty(String name, JsonNode objectNode) {
    JsonNode propertyNode = null;
    if (objectNode.get(EDITOR_SHAPE_PROPERTIES) != null) {
      JsonNode propertiesNode = objectNode.get(EDITOR_SHAPE_PROPERTIES);
      propertyNode = propertiesNode.get(name);
    }
    return propertyNode;
  }
  
}
