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

import java.util.ArrayList;
import java.util.List;

/**
 * @author jbarrez
 */
public class ReviveModelResultRepresentation {
	
	private List<UnresolveModelRepresentation> unresolvedModels = new ArrayList<UnresolveModelRepresentation>();
	
	public List<UnresolveModelRepresentation> getUnresolvedModels() {
		return unresolvedModels;
	}

	public void setUnresolvedModels(List<UnresolveModelRepresentation> unresolvedModels) {
		this.unresolvedModels = unresolvedModels;
	}

	public static class UnresolveModelRepresentation {
		
		private String id;
		private String name;
		private String createdBy;
		
		public UnresolveModelRepresentation(String id, String name, String createdBy) {
	        this.id = id;
	        this.name = name;
	        this.createdBy = createdBy;
        }
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getCreatedBy() {
			return createdBy;
		}
		public void setCreatedBy(String createdBy) {
			this.createdBy = createdBy;
		}
		
	}

}
