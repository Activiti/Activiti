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
package org.activiti.rest.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.extensions.surf.util.Base64;
import org.springframework.extensions.webscripts.Authenticator;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.webscripts.servlet.ServletAuthenticatorFactory;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;

/**
 * HTTP Basic Authentication
 *
 * Abstract class that makes basic authentication easier to handle by managing the request headers
 * and the base 64 decoding.
 *
 * Extend this class and implement doAuthenticate and doAuthorize to authenticate and authorize against a specific
 * user store.
 *
 * @author Erik Winlof
 */
public abstract class AbstractBasicHttpAuthenticatorFactory implements ServletAuthenticatorFactory
{

  protected String guestUserName = "Guest";
  protected String basicRealm = "WebScripts";

  /**
   * Factory's create method.
   *
   * @param req The webscript request
   * @param res THe webscript response
   * @return A BasicHttpAuthenticator instance
   */
  public Authenticator create(WebScriptServletRequest req, WebScriptServletResponse res)
  {
    return new BasicHttpAuthenticator(req, res);
  }

  /**
   * Implement to authenticate against a specific user store.
   *
   * @param username The username
   * @param password The password
   * @return Shall return true if authentication was successful
   */
  public abstract boolean doAuthenticate(String username, String password);

  /**
   * Implement to authorize against a specific user store.
   *
   * @param username The username
   * @param role The role that the user MUST have
   * @return Shall return true if the user has the given role
   */
  public abstract boolean doAuthorize(String username, RequiredAuthentication role);
  
  /**
   * HTTP Basic Authentication
   *
   * @author Erik Winlof
   */
  public class BasicHttpAuthenticator implements Authenticator {

    /**
     * The webscript servlet request
     */
    private WebScriptServletRequest servletReq;

    /**
     * The webscript servlet response
     */
    private WebScriptServletResponse servletRes;

    /**
     * The "raw" value of the "Authorization" header in the http request
     */
    private String authorization;

    /**
     * Construct
     */
    public BasicHttpAuthenticator(WebScriptServletRequest req, WebScriptServletResponse res)
    {
      this.servletReq = req;
      this.servletRes = res;

      HttpServletRequest httpReq = servletReq.getHttpServletRequest();

      this.authorization = httpReq.getHeader("Authorization");
    }

    /**
     * Checks that the user has access to the webscript.
     *
     * @param required Required level of authentication
     * @param isGuest true if a Guest is accessing the web script
     *
     * @return true if webscript has "none" or "guest" authentication OR
     *              user and password match and user has requested role
     */
    public boolean authenticate(RequiredAuthentication required, boolean isGuest)
    {
      boolean authenticated = false;

      HttpServletResponse res = servletRes.getHttpServletResponse();

      // Authenticate as guest, if service allows
      if (isGuest && RequiredAuthentication.guest == required)
      {
        authenticated = true;
      }

      // Authenticate as specified by HTTP Basic Authentication
      else if (authorization != null && authorization.length() > 0)
      {
        try
        {
          // Decode the authorization header
          String[] authorizationParts = authorization.split(" ");
          if (!authorizationParts[0].equalsIgnoreCase("basic"))
          {
            throw new WebScriptException("Authorization '" + authorizationParts[0] + "' not supported.");
          }
          String decodedAuthorisation = new String(Base64.decode(authorizationParts[1]));
          String[] parts = decodedAuthorisation.split(":");

          if (parts.length == 2)
          {
            try
            {
              // Authenticate and authorize
              authenticated = doAuthenticate(parts[0], parts[1]);
              if (authenticated) {
                if (!doAuthorize(parts[0], required)) {
                  authenticated = false;
                }
              }
            }
            catch(AuthenticationException ae)
            {
              throw ae;
            }
          }
        }
        catch(RuntimeException e)
        {
          // failed authentication
        }
      }

      if (!authenticated)
      {
        // Set response to signal that the user wasn't authenticated to access the webscript
        res.setStatus(401);
        res.setHeader("WWW-Authenticate", "Basic realm=\"" + basicRealm + "\"");
      }
      return authenticated;
    }

    /**
     * Check if the credentials are empty.
     *
     * @return true if the credentials are emtpy
     */
    public boolean emptyCredentials()
    {
      return (authorization == null || authorization.length() == 0);
    }
  }

  /**
   * The guest username.
   *
   * @param guestUserName The guest username
   */
  public void setGuestUserName(String guestUserName) {
    this.guestUserName = guestUserName;
  }

  /**
   * The basic realm.
   *
   * @param basicRealm The basic realm
   */
  public void setBasicRealm(String basicRealm) {
    this.basicRealm = basicRealm;
  }

}