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
package com.activiti.rest.idm;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.activiti.model.common.ImageUploadRepresentation;
import com.activiti.model.idm.ChangePasswordRepresentation;
import com.activiti.model.idm.UserRepresentation;

/**
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
@RestController
@RequestMapping(value="/rest/admin")
public class IdmProfileResource extends AbstractIdmProfileResource {
    
    @RequestMapping(value="/profile", method = RequestMethod.GET, produces = "application/json")
    public UserRepresentation getProfile() {
    	return super.getProfile();
    }
    
    @RequestMapping(value="/profile", method = RequestMethod.POST, produces = "application/json")
    public UserRepresentation updateUser(@RequestBody UserRepresentation userRepresentation) {
    	return super.updateUser(userRepresentation);
    }
    
    @RequestMapping(value="/profile-picture", method = RequestMethod.GET)
    public void getProfilePicture(HttpServletResponse response) {
    	super.getProfilePicture(response);
    }
    
    @RequestMapping(value="/profile-picture", method = RequestMethod.POST, produces = "application/json")
	public ImageUploadRepresentation uploadProfilePicture(@RequestParam("file") MultipartFile file) {
		return super.uploadProfilePicture(file);
	}
    
    @RequestMapping(value="/profile-password", method = RequestMethod.POST, produces = "application/json")
    public void changePassword(@RequestBody ChangePasswordRepresentation changePasswordRepresentation) {
    	super.changePassword(changePasswordRepresentation);
    }
    
}
