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
package org.activiti.app.model.editor;

import org.activiti.app.domain.editor.AbstractModel;
import org.activiti.app.domain.editor.AppDefinition;



/**
 * Representation of app definitions to be used in the model overview.
 * 
 * @author Tijs Rademakers
 */
public class AppDefinitionListModelRepresentation extends ModelRepresentation {
	
	protected AppDefinition appDefinition;
	
	public AppDefinitionListModelRepresentation(AbstractModel model) {
	    initialize(model);	
	}
	
	public AppDefinitionListModelRepresentation() {
	    
	}

	public AppDefinition getAppDefinition() {
		return appDefinition;
	}

	public void setAppDefinition(AppDefinition appDefinition) {
		this.appDefinition = appDefinition;
	}
}
