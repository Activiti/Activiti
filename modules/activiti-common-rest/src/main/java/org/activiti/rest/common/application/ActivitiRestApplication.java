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

package org.activiti.rest.common.application;

import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.filter.RestAuthenticator;
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
  protected RestAuthenticator restAuthenticator;

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
  
  public void setRestAuthenticator(RestAuthenticator restAuthenticator) {
    this.restAuthenticator = restAuthenticator;
  }

  public void setMediaTypeResolver(MediaTypeResolver mediaTypeResolver) {
    this.mediaTypeResolver = mediaTypeResolver;
  }
  
  public void initializeAuthentication() {
    Verifier verifier = new SecretVerifier() {

      @Override
      public int verify(String username, char[] password) throws IllegalArgumentException {
        boolean verified = ActivitiUtil.getIdentityService().checkPassword(username, new String(password));
        if (verified) {
          return RESULT_VALID;
        } else {
          return RESULT_INVALID;
        }
      }
    };
    
    // Set authenticator as a NON-optional filter. If certain request require no authentication, a custom RestAuthenticator
    // should be used to free the request from authentication.
    authenticator = new ChallengeAuthenticator(null, true, ChallengeScheme.HTTP_BASIC,
          "Activiti Realm") {
      
      @Override
      protected boolean authenticate(Request request, Response response) {
        
        // Check if authentication is required if a custom RestAuthenticator is set
        if(restAuthenticator != null && !restAuthenticator.requestRequiresAuthentication(request)) {
          return true;
        }
        
        if (request.getChallengeResponse() == null) {
          return false;
        } else {
          boolean authenticated = super.authenticate(request, response);
          if(authenticated && restAuthenticator != null) {
            // Additional check to see if authenticated user is authorised. By default, when no RestAuthenticator
            // is set, a valid user can perform any request.
            authenticated = restAuthenticator.isRequestAuthorized(request);
          }
          return authenticated;
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
