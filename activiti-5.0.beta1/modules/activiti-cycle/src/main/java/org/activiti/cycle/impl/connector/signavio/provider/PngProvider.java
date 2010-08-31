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
import org.activiti.cycle.ContentType;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;

public class PngProvider extends SignavioContentRepresentationProvider {

  public static final String NAME = "PNG";

  public PngProvider() {
    super(NAME, ContentType.PNG, false);
  }

  @Override
  public void addValueToContent(Content content, RepositoryArtifact artifact) {
    try {
      String modelAsPngUrl = getModelAsPngUrl(artifact);
      InputStream is = new URL(modelAsPngUrl).openStream();

      content.setValue(is);
    } catch (Exception ex) {
      throw new RepositoryException("Exception while accessing Signavio repository", ex);
    }
  }

  /**
   * for documentation (even if do not use it at the moment)
   */
  public String getModelAsPngUrl(RepositoryArtifact fileInfo) {
    return getConnector(fileInfo).getConfiguration().getModelUrl(fileInfo.getId()) + "/png?token=" + getConnector(fileInfo).getSecurityToken();
  }

}
