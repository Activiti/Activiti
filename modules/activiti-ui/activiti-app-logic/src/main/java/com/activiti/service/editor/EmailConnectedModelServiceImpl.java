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
package com.activiti.service.editor;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.activiti.domain.idm.User;
import com.activiti.repository.editor.ModelShareInfoRepository;
import com.activiti.service.api.EmailConnectedModelService;

/**
 * @author jbarrez
 */
@Service
@Transactional
public class EmailConnectedModelServiceImpl implements EmailConnectedModelService {
	
	@Inject
	private ModelShareInfoRepository modelShareInfoRepository;
	
	@Override
	public void connectSharedModelsByEmail(String email, User user) {
		modelShareInfoRepository.connectSharedModelsByEmail(email, user);
	}
	
}
