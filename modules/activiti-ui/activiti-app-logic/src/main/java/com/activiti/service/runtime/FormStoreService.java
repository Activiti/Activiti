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
package com.activiti.service.runtime;

import org.springframework.data.domain.Page;

import com.activiti.domain.idm.User;
import com.activiti.domain.runtime.Form;

/**
 * @author Joram Barrez
 */
public interface FormStoreService {

	Form getForm(String formKey);
	
	Form saveForm(Form form);
	
	void generateMetrics();
	
	/**
	 * @return all forms for the given user, optionally filtered based on the given filter. If filter is null
	 * or empty, all forms are returned (limited by page-parameters).
	 */
	Page<Form> filterForms(User user, String filter, int page, int pageSize);
	
}
