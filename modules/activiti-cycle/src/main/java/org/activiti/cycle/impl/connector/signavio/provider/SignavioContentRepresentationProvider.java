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

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorInterface;
import org.restlet.Response;

public abstract class SignavioContentRepresentationProvider implements ContentRepresentation {

  private static final long serialVersionUID = 1L;

  public static Response getJsonResponse(SignavioConnectorInterface connector, RepositoryArtifact artifact, String urlSuffix) throws IOException {
    String url = connector.getModelUrl(artifact) + urlSuffix;
    return connector.getJsonResponse(url);
  }

}
