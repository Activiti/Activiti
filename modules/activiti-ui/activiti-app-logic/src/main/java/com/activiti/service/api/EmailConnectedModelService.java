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
package com.activiti.service.api;

import com.activiti.domain.idm.User;

/**
 * @author jbarrez
 */
public interface EmailConnectedModelService {

	/**
	 * Called when a {@link User} has been made for a user that didn't exist before
	 * (and hence the model was assigned by email). This operation replaces that email
	 * with a real user entity for the models. 
	 */
	void connectSharedModelsByEmail(String email, User user);
	
}
