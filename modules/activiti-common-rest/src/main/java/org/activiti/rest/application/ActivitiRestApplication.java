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

package org.activiti.rest.application;

import org.activiti.rest.api.ActivitiUtil;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeScheme;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.SecretVerifier;
import org.restlet.security.Verifier;
/**
 * @author Tijs Rademakers
 */
public abstract class ActivitiRestApplication extends Application {
  
  protected ChallengeAuthenticator authenticator;
  protected ActivitiStatusService activitiStatusService;
  protected MediaTypeResolver mediaTypeResolver;

  public ActivitiRestApplication() {
    activitiStatusService = new ActivitiStatusService();
    setStatusService(activitiStatusService);
  }
  
  public MediaTypeResolver getMediaTypeResolver() {
    if(mediaTypeResolver == null) {
      // Revert to default implementation when no custom resolver has been set
      mediaTypeResolver = new DefaultMediaTypeResolver();
    }
    
    return mediaTypeResolver;
  }

  public void setMediaTypeResolver(MediaTypeResolver mediaTypeResolver) {
    this.mediaTypeResolver = mediaTypeResolver;
  }
  
  public void initializeAuthentication() {
    Verifier verifier = new SecretVerifier() {

      @Override
      public boolean verify(String username, char[] password) throws IllegalArgumentException {
        boolean verified = ActivitiUtil.getIdentityService().checkPassword(username, new String(password));
        return verified;
      }
    };
    authenticator = new ChallengeAuthenticator(null, true, ChallengeScheme.HTTP_BASIC,
          "Activiti Realm") {
      
      @Override
      protected boolean authenticate(Request request, Response response) {
        if (request.getChallengeResponse() == null) {
          return false;
        } else {
          return super.authenticate(request, response);
        }
      }
    };
    authenticator.setVerifier(verifier);
  }
  
  public String authenticate(Request request, Response response) {
    if (!request.getClientInfo().isAuthenticated()) {
      authenticator.challenge(response, false);
      return null;
    }
    return request.getClientInfo().getUser().getIdentifier();
  }
}
