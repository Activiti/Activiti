/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.activiti.model.editor;

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
		
		private Long id;
		private String name;
		private String createdBy;
		
		public UnresolveModelRepresentation(Long id, String name, String createdBy) {
	        this.id = id;
	        this.name = name;
	        this.createdBy = createdBy;
        }
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
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
