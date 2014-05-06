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

import org.activiti.editor.constants.EditorJsonConstants;
import org.activiti.editor.constants.StencilConstants;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class BpmnJsonConverterUtil implements EditorJsonConstants, StencilConstants {
  
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
  
  protected static String getPropertyValueAsString(String name, JsonNode objectNode) {
    String propertyValue = null;
    JsonNode propertyNode = getProperty(name, objectNode);
    if (propertyNode != null) {
      propertyValue = propertyNode.asText();
    }
    return propertyValue;
  }
  
  protected static JsonNode getProperty(String name, JsonNode objectNode) {
    JsonNode propertyNode = null;
    if (objectNode.get(EDITOR_SHAPE_PROPERTIES) != null) {
      JsonNode propertiesNode = objectNode.get(EDITOR_SHAPE_PROPERTIES);
      propertyNode = propertiesNode.get(name);
    }
    return propertyNode;
  }
  
}
