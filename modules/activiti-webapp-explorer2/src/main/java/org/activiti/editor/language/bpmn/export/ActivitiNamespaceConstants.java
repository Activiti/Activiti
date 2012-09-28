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

/**
 * @author Tijs Rademakers
 */
public interface ActivitiNamespaceConstants {
  
  public static final String BPMN2_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";
  public static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
  public static final String SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
  public static final String XPATH_NAMESPACE = "http://www.w3.org/1999/XPath";
  public static final String PROCESS_NAMESPACE = "http://www.activiti.org/test";
  public static final String ACTIVITI_EXTENSIONS_NAMESPACE = "http://activiti.org/bpmn";
  public static final String ACTIVITI_EXTENSIONS_PREFIX = "activiti";
  public static final String BPMNDI_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/DI";
  public static final String BPMNDI_PREFIX = "bpmndi";
  public static final String OMGDC_NAMESPACE = "http://www.omg.org/spec/DD/20100524/DC";
  public static final String OMGDC_PREFIX = "omgdc";
  public static final String OMGDI_NAMESPACE = "http://www.omg.org/spec/DD/20100524/DI";
  public static final String OMGDI_PREFIX = "omgdi";
  
  public static final String CLASS_TYPE = "class";
  public static final String EXPRESSION_TYPE = "expression";
  public static final String DELEGATE_EXPRESSION_TYPE = "delegateexpression";
  public static final String ALFRESCO_TYPE = "alfrescoScriptType";
  public static final String EXECUTION_LISTENER = "executionListener";
  public static final String TASK_LISTENER = "taskListener";

}
