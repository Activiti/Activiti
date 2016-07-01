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
package com.activiti.service.activiti;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppVersionClientService extends AbstractEncryptingService {

	@Autowired
    protected ActivitiClientService clientUtil;

    public String getEndpointTypeUsingEncryptedPassword(String contextRoot, String restRoot,
                                  String serverAddress, Integer port,
                                  String userName, String encryptedPassword) {
        String decryptedPassword = decrypt(encryptedPassword);
        return getEndpointType(contextRoot, restRoot, serverAddress, port, userName, decryptedPassword);
    }

    public String getEndpointType(String contextRoot, String restRoot,
                                  String serverAddress, Integer port,
                                  String userName, String password) {
        String result = null;
        HttpGet get = new HttpGet(clientUtil.getServerUrl(contextRoot, restRoot, serverAddress, port, "enterprise/app-version"));

        try {
            JsonNode jsonNode = clientUtil.executeRequest(get, userName, password);

            if (jsonNode != null && jsonNode.has("type")) {
                result = jsonNode.get("type").asText();
            }
        } catch (Exception ex) {
            // could not reach configured server
        }

        return result;
    }
}
