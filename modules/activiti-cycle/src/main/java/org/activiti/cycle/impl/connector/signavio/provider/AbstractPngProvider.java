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

import java.io.InputStream;
import java.net.URL;

import org.activiti.cycle.Content;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.RenderInfo;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleSessionContext;
import org.activiti.cycle.impl.components.RuntimeConnectorList;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorConfiguration;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorInterface;
import org.activiti.cycle.impl.mimetype.PngMimeType;

public abstract class AbstractPngProvider extends SignavioContentRepresentationProvider {

  private static final long serialVersionUID = 1L;

  public Content getContent(RepositoryArtifact artifact) {
    try {
      SignavioConnectorInterface signavioConnector = (SignavioConnectorInterface) CycleSessionContext.get(RuntimeConnectorList.class).getConnectorById(
              artifact.getConnectorId());
      Content content = new Content();
      SignavioConnectorConfiguration configuration = (SignavioConnectorConfiguration) signavioConnector.getConfiguration();
      String modelAsPngUrl = configuration.getPngUrl(artifact.getNodeId(), signavioConnector.getSecurityToken());
      InputStream is = new URL(modelAsPngUrl).openStream();
      content.setValue(is);
      return content;
    } catch (Exception ex) {
      throw new RepositoryException("Exception while accessing Signavio repository", ex);
    }
  }

  public String getId() {
    return "PNG";
  }

  public MimeType getRepresentationMimeType() {
    return CycleApplicationContext.get(PngMimeType.class);
  }

  public RenderInfo getRenderInfo() {
    return RenderInfo.IMAGE;
  }
  
  public boolean isForDownload() {
    return true;
  }

}
