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
package org.activiti.workflow.simple.alfresco.conversion;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.workflow.simple.alfresco.conversion.script.PropertyReference;
import org.activiti.workflow.simple.alfresco.conversion.script.ScriptTaskListenerBuilder;
import org.activiti.workflow.simple.alfresco.model.M2Model;
import org.activiti.workflow.simple.alfresco.model.M2Namespace;
import org.activiti.workflow.simple.alfresco.model.config.Extension;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;

/**
 * Util class that contains methods related to Alfresco-artifacts used in
 * workflow definition conversion.
 * 
 * @author Frederik Heremans
 */
public class AlfrescoConversionUtil implements AlfrescoConversionConstants {

	/**
	 * @return a valid string to use as id, based on the given String. All invalid
	 * characters are removed.
	 */
	public static String getValidIdString(String s) {
		if(s != null) {
			return s.toLowerCase().replace(" ", "").replace("_", "").replace("-", "_");
		}
		return null;
	}
	
	/**
	 * @param prefix namespace prefix
	 * @param name local name. All characters that are not allowed are removed from the given value,
	 * @see #getValidIdString(String)
	 * @return fully qualified name
	 */
	public static String getQualifiedName(String prefix, String name) {
		return prefix + ":" + getValidIdString(name);
	}
	
	public static M2Namespace createNamespace(String prefix) {
		String uri  = MessageFormat.format(CONTENT_MODEL_NAMESPACE_URL, prefix);
		return new M2Namespace(uri, prefix);
	}
	
	public static String getUrlQualifiedPropertyName(String prefixedProperty, M2Namespace nameSpace) {
		return "{" + nameSpace.getUri() + "}" + prefixedProperty.replace(nameSpace.getPrefix() +":", "");
	}
	
	// Artifact related methods
	public static void storeContentModel(M2Model model, WorkflowDefinitionConversion conversion) {
		conversion.setArtifact(ARTIFACT_CONTENT_MODEL_KEY, model);
	}
	
	public static M2Model getContentModel(WorkflowDefinitionConversion conversion) {
		return (M2Model) conversion.getArtifact(ARTIFACT_CONTENT_MODEL_KEY);
	}

	public static void storeExtension(Extension module, WorkflowDefinitionConversion conversion) {
		conversion.setArtifact(ARTIFACT_SHARE_CONFIG_EXTENSION, module);
  }
	
	public static Extension getExtension(WorkflowDefinitionConversion conversion) {
		return (Extension) conversion.getArtifact(ARTIFACT_SHARE_CONFIG_EXTENSION);
	}
	
	public static void storeModelNamespacePrefix(String prefix, WorkflowDefinitionConversion conversion) {
		conversion.setArtifact(ARTIFACT_MODEL_NAMESPACE_PREFIX, prefix);
	}
	
	public static String getModelNamespacePrefix(WorkflowDefinitionConversion conversion) {
		return (String) conversion.getArtifact(ARTIFACT_MODEL_NAMESPACE_PREFIX);
	}
	
	@SuppressWarnings("unchecked")
  public static List<PropertyReference> getPropertyReferences(WorkflowDefinitionConversion conversion) {
		return (List<PropertyReference>) conversion.getArtifact(ARTIFACT_PROPERTY_REFERENCES);
	}
	
	/**
	 * @return the {@link PropertySharing} object for the given usertask-id. Creates and registers a new
	 * object if there is no {@link PropertySharing} object registered for the given usertask.
	 */
	@SuppressWarnings("unchecked")
  public static PropertySharing getPropertySharing(WorkflowDefinitionConversion conversion, String userTaskId) {
		List<PropertySharing> sharingList = (List<PropertySharing>) conversion.getArtifact(ARTIFACT_PROPERTY_SHARING);
		if(sharingList == null) {
			sharingList = new ArrayList<PropertySharing>();
			conversion.setArtifact(ARTIFACT_PROPERTY_SHARING, sharingList);
		}
		
		PropertySharing result = null;
		for(PropertySharing sharing : sharingList) {
			if(userTaskId.equals(sharing.getUserTaskId())) {
				result = sharing;
			}
		}
		
		if(result == null) {
			result = new PropertySharing();
			result.setUserTaskId(userTaskId);
			sharingList.add(result);
		}
		return result;
	}
	
	/**
	 * @return the {@link ScriptTaskListenerBuilder} object for the given usertask-id. Creates and registers a new
	 * object if there is no {@link ScriptTaskListenerBuilder} object registered for the given usertask.
	 */
	@SuppressWarnings("unchecked")
  public static ScriptTaskListenerBuilder getScriptTaskListenerBuilder(WorkflowDefinitionConversion conversion, String userTaskId, String eventName) {
		String key = userTaskId + "-" + eventName;
		Map<String, ScriptTaskListenerBuilder> builderMap = (Map<String, ScriptTaskListenerBuilder>) conversion.getArtifact(ARTIFACT_PROPERTY_TASK_SCRIPT_BUILDER);
		if(builderMap == null) {
			builderMap = new HashMap<String, ScriptTaskListenerBuilder>();
			conversion.setArtifact(ARTIFACT_PROPERTY_TASK_SCRIPT_BUILDER, builderMap);
		}
		
		ScriptTaskListenerBuilder result = builderMap.get(key);
		
		if(result == null) {
			result = new ScriptTaskListenerBuilder();
			result.setEvent(eventName);
			builderMap.put(key, result);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
  public static boolean hasTaskScriptTaskListenerBuilder(WorkflowDefinitionConversion conversion, String userTaskId, String eventName) {
		String key = userTaskId + "-" + eventName;
		Map<String, ScriptTaskListenerBuilder> builderMap = (Map<String, ScriptTaskListenerBuilder>) conversion.getArtifact(ARTIFACT_PROPERTY_TASK_SCRIPT_BUILDER);
		if(builderMap != null) {
			return builderMap.get(key) != null;
		}		
		return false;
	}
	
}
