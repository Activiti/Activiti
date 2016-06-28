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
package com.activiti.model.idm;

import com.activiti.domain.idm.Group;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Joram Barrez
 */
public class LightGroupRepresentation extends AbstractGroupRepresentation {
	
	protected List<LightGroupRepresentation> groups;
	
	public LightGroupRepresentation() {
		
	}
	
	public LightGroupRepresentation(Group group) {
	    super(group);
	}

	public void addGroup(LightGroupRepresentation group) {
		if (groups == null) {
			groups = new ArrayList<LightGroupRepresentation>();
		}
		groups.add(group);
	}

	public List<LightGroupRepresentation> getGroups() {
		return groups;
	}

	public void setGroups(List<LightGroupRepresentation> groups) {
		this.groups = groups;
	}
	
}
