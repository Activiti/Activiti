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
package org.activiti.engine.impl.bpmn.deployer;

import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Static methods for working with BPMN and image resource names.
 */
public class ResourceNameUtil {
  
  public static final String[] BPMN_RESOURCE_SUFFIXES = new String[] { "bpmn20.xml", "bpmn" };
  public static final String[] DIAGRAM_SUFFIXES = new String[] { "png", "jpg", "gif", "svg" };

  public static String stripBpmnFileSuffix(String bpmnFileResource) {
    for (String suffix : BPMN_RESOURCE_SUFFIXES) {
      if (bpmnFileResource.endsWith(suffix)) {
        return bpmnFileResource.substring(0, bpmnFileResource.length() - suffix.length());
      }
    }
    return bpmnFileResource;
  }

  public static String getProcessDiagramResourceName(String bpmnFileResource, String processKey, String diagramSuffix) {
    String bpmnFileResourceBase = ResourceNameUtil.stripBpmnFileSuffix(bpmnFileResource);
    return bpmnFileResourceBase + processKey + "." + diagramSuffix;
  }
  
  /**
   * Finds the name of a resource for the diagram for a process definition.  Assumes that the
   * process definition's key and (BPMN) resource name are already set.
   *
   * <p>It will first look for an image resource which matches the process specifically, before
   * resorting to an image resource which matches the BPMN 2.0 xml file resource.
   *
   * <p>Example: if the deployment contains a BPMN 2.0 xml resource called 'abc.bpmn20.xml'
   * containing only one process with key 'myProcess', then this method will look for an image resources
   * called'abc.myProcess.png' (or .jpg, or .gif, etc.) or 'abc.png' if the previous one wasn't
   * found.
   *
   * <p>Example 2: if the deployment contains a BPMN 2.0 xml resource called 'abc.bpmn20.xml'
   * containing three processes (with keys a, b and c), then this method will first look for an image resource
   * called 'abc.a.png' before looking for 'abc.png' (likewise for b and c). Note that if abc.a.png,
   * abc.b.png and abc.c.png don't exist, all processes will have the same image: abc.png.
   *
   * @return name of an existing resource, or null if no matching image resource is found in the resources.
   */
  public static String getProcessDiagramResourceNameFromDeployment(
      ProcessDefinitionEntity processDefinition, Map<String, ResourceEntity> resources) {
    
    if (StringUtils.isEmpty(processDefinition.getResourceName())) {
      throw new IllegalStateException("Provided process definition must have its resource name set.");
    }
    
    String bpmnResourceBase = stripBpmnFileSuffix(processDefinition.getResourceName());
    String key = processDefinition.getKey();
    
    for (String diagramSuffix : DIAGRAM_SUFFIXES) {
      String possibleName = bpmnResourceBase + key + "." + diagramSuffix;
      if (resources.containsKey(possibleName)) {
        return possibleName;
      }
      
      possibleName = bpmnResourceBase + diagramSuffix;
      if (resources.containsKey(possibleName)) {
        return possibleName;
      }
    }
    
    return null;
  }
  
}

