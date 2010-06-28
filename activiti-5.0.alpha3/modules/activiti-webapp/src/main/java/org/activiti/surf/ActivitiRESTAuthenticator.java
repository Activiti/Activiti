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
package org.activiti.surf;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.exception.AuthenticationException;
import org.springframework.extensions.webscripts.connector.*;

/**
 * Connects to a activiti rest endpoint's login webscript using json format.
 *
 * @author Erik Winlšf
 */
public class ActivitiRESTAuthenticator extends AbstractAuthenticator
{

  /**
   * Key user to save value in connector session
   */
  public final static String ACTIVITI_REST_AUTHORISED_KEY = "org.activiti.rest.authorised";

  /**
   *
   * @param endpoint The endpoint to authenticate against
   * @param credentials The credentials used for the authentication
   * @param connectorSession The connector session
   * @return The ConnectorSession
   * @throws AuthenticationException
   */
  public ConnectorSession authenticate(String endpoint, Credentials credentials, ConnectorSession connectorSession)
    throws AuthenticationException
  {
    ConnectorSession cs = null;

    if (credentials != null)
    {
      // build a new remote client
      RemoteClient remoteClient = new RemoteClient(endpoint);

      // retrieve the username and password
      String userId = (String) credentials.getProperty(Credentials.CREDENTIAL_USERNAME);
      String password = (String) credentials.getProperty(Credentials.CREDENTIAL_PASSWORD);

      // POST to the login WebScript
      remoteClient.setRequestContentType("application/json");
      JSONObject json = new JSONObject();
      try {
        json.put("userId", userId);
        json.put("password", password);
      }
      catch (JSONException e)
      {
        throw new AuthenticationException("Unable to create json login request", e);
      }
      Response response = remoteClient.call("/login", json.toString());

      // read back the ticket
      if (response.getStatus().getCode() == 200)
      {
        try
        {
          json = new JSONObject(response.getResponse());
          if (json.getBoolean("success"))
          {
            if (connectorSession != null)
            {
              // Mark session as authenticated and signal as succeeded
              connectorSession.setParameter(ACTIVITI_REST_AUTHORISED_KEY, "true");
              cs = connectorSession;
            }
          }
        }
        catch (JSONException e)
        {
          // the session that came back could not be parsed
          // this will cause the entire handshake to fail
          throw new AuthenticationException("Unable to validate login", e);
        }
      }
      else
      {
        // Authentication failed, received response code: " + response.getStatus().getCode());
        try
        {
          json = new JSONObject(response.getResponse());
          throw new AuthenticationException(json.getString("message"));
        }
        catch (JSONException e)
        {
          throw new AuthenticationException("Unable to login:" + response.getResponse());

        }
      }
    }
    else
    {
      // No user credentials available - cannot authenticate
      throw new AuthenticationException("Unable to validate login since username and password wasn't provided");
    }
    return cs;
  }

  /**
   * Check if user is authenticated.
   *
   * @param endpoint The enpoint
   * @param connectorSession The connector session
   * @return true if user is authenticated
   */
  public boolean isAuthenticated(String endpoint, ConnectorSession connectorSession)
  {
    return (connectorSession.getParameter(ACTIVITI_REST_AUTHORISED_KEY) != null);
  }
}