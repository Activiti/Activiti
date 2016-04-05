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
package org.activiti.dmn.xml.constants;

/**
 * @author Tijs Rademakers
 */
public interface DmnXMLConstants {

  public static final String DMN_NAMESPACE = "http://www.omg.org/spec/DMN/20151130";
  public static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
  public static final String XSI_PREFIX = "xsi";
  public static final String SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
  public static final String MODEL_NAMESPACE = "http://www.activiti.org/dmn";
  public static final String TARGET_NAMESPACE_ATTRIBUTE = "targetNamespace";
  public static final String ACTIVITI_EXTENSIONS_NAMESPACE = "http://activiti.org/dmn";
  public static final String ACTIVITI_EXTENSIONS_PREFIX = "activiti";

  public static final String ATTRIBUTE_ID = "id";
  public static final String ATTRIBUTE_NAME = "name";
  public static final String ATTRIBUTE_LABEL = "label";
  public static final String ATTRIBUTE_TYPE_REF = "typeRef";
  public static final String ATTRIBUTE_HREF = "href";
  public static final String ATTRIBUTE_HIT_POLICY = "hitPolicy";
  public static final String ATTRIBUTE_NAMESPACE = "namespace";

  public static final String ELEMENT_DEFINITIONS = "definitions";
  public static final String ELEMENT_DECISION = "decision";
  public static final String ELEMENT_DECISION_TABLE = "decisionTable";

  public static final String ELEMENT_ITEM_DEFINITION = "itemDefinition";
  public static final String ELEMENT_TYPE_DEFINITION = "typeDefinition";

  public static final String ELEMENT_INPUT_CLAUSE = "input";
  public static final String ELEMENT_OUTPUT_CLAUSE = "output";
  public static final String ELEMENT_INPUT_EXPRESSION = "inputExpression";
  public static final String ELEMENT_TEXT = "text";

  public static final String ELEMENT_RULE = "rule";
  public static final String ELEMENT_INPUT_ENTRY = "inputEntry";
  public static final String ELEMENT_OUTPUT_ENTRY = "outputEntry";
  
  public static final String ELEMENT_DESCRIPTION = "description";
  public static final String ELEMENT_EXTENSIONS = "extensionElements";
}
