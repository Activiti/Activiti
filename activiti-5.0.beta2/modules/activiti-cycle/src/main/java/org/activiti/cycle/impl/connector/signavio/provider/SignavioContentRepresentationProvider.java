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

import java.io.IOException;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.ContentProviderImpl;
import org.activiti.cycle.impl.connector.signavio.SignavioConnector;
import org.restlet.data.Response;

public abstract class SignavioContentRepresentationProvider extends ContentProviderImpl {

  public Response getJsonResponse(SignavioConnector connector, RepositoryArtifact artifact, String urlSuffix) throws IOException {
    String url = connector.getModelUrl(artifact) + urlSuffix;
    return connector.getJsonResponse(url);
  }

  @Override
  public void addValueToContent(Content content, RepositoryConnector connector, RepositoryArtifact artifact) {
    addValueToContent(content, (SignavioConnector) connector, artifact);
  }
  
  public abstract void addValueToContent(Content content, SignavioConnector connector, RepositoryArtifact artifact);  
}
