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

package org.activiti.rest.api.identity;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.Picture;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.data.CacheDirective;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
public class UserPictureResource extends SecuredResource {
  
  @Get
  public InputRepresentation getPicture() {
    if(authenticate() == false) return null;
    
    String userId = (String) getRequest().getAttributes().get("userId");
    if(userId == null) {
      throw new ActivitiException("No userId provided");
    }
    Picture picture = ActivitiUtil.getIdentityService().getUserPicture(userId);
    
    String contentType = picture.getMimeType();
    MediaType mediatType = MediaType.IMAGE_PNG;
    if(contentType != null) {
      if(contentType.contains(";")) {
        contentType = contentType.substring(0, contentType.indexOf(";"));
      }
      mediatType = MediaType.valueOf(contentType);
    }
    InputRepresentation output = new InputRepresentation(picture.getInputStream(), mediatType);
    getResponse().getCacheDirectives().add(CacheDirective.maxAge(28800));
    
    return output;
  }

}
