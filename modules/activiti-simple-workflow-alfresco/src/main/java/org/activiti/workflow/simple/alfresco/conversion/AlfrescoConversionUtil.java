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

import org.activiti.workflow.simple.alfresco.model.M2Model;
import org.activiti.workflow.simple.alfresco.model.M2Namespace;
import org.activiti.workflow.simple.alfresco.model.config.Module;
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
			return s.toLowerCase().replace(" ", "").replace("_", "");
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
	
	// Artifact related methods
	public static void storeContentModel(M2Model model, WorkflowDefinitionConversion conversion) {
		conversion.setArtifact(ARTIFACT_CONTENT_MODEL_KEY, model);
	}
	
	public static M2Model getContentModel(WorkflowDefinitionConversion conversion) {
		return (M2Model) conversion.getArtifact(ARTIFACT_CONTENT_MODEL_KEY);
	}

	public static void storeModule(Module module, WorkflowDefinitionConversion conversion) {
		conversion.setArtifact(ARTIFACT_SHARE_CONFIG_MODULE, module);
  }
	
	public static Module getModule(WorkflowDefinitionConversion conversion) {
		return (Module) conversion.getArtifact(ARTIFACT_SHARE_CONFIG_MODULE);
	}
	
	public static void storeModelNamespacePrefix(String prefix, WorkflowDefinitionConversion conversion) {
		conversion.setArtifact(ARTIFACT_MODEL_NAMESPACE_PREFIX, prefix);
	}
	
	public static String getModelNamespacePrefix(WorkflowDefinitionConversion conversion) {
		return (String) conversion.getArtifact(ARTIFACT_MODEL_NAMESPACE_PREFIX);
	}
	
}
