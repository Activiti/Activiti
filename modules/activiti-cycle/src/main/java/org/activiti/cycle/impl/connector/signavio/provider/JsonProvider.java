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
package org.activiti.cycle.impl.connector.signavio.provider;

import org.activiti.cycle.Content;
import org.activiti.cycle.ContentType;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;
import org.json.JSONObject;
import org.restlet.data.Response;

public class JsonProvider extends SignavioContentRepresentationProvider {

  /**
   * TODO: Think about the names and if we want to store them here or manage
   * them centrally?
   */
  public static final String NAME = "json";

  public JsonProvider() {
    super(NAME, ContentType.TEXT, false);
  }

  @Override
  public void addValueToContent(Content content, RepositoryArtifact artifact) {
    try {
      Response jsonResponse = getJsonResponse(artifact, "/json");
      
      String jsonString = jsonResponse.getEntity().getText();
      JSONObject jsonObj = new JSONObject(jsonString);
      content.setValue(jsonObj.toString(2));
    } catch (Exception ex) {
      throw new RepositoryException("Error while accessing Signavio repository", ex);
    }
  }

}
