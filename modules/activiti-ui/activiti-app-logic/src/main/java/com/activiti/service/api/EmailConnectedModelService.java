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
