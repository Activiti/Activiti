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

package org.activiti.rest.api.cycle.dto;

import org.activiti.cycle.RepositoryArtifact;

/**
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class TreeArtifactDto extends TreeNodeDto {

  private final String file = String.valueOf(Boolean.TRUE);
  private final String labelStyle;

  public TreeArtifactDto(RepositoryArtifact artifact) {
    super(artifact);
    this.expanded = String.valueOf(Boolean.TRUE);
    if (artifact.getArtifactType().getName().equals("image/png") || artifact.getArtifactType().getName().equals("image/gif")
            || artifact.getArtifactType().getName().equals("image/jpeg")) {
      this.labelStyle = "icon-img";
    } else if (artifact.getArtifactType().getName().equals("application/xml")) {
      this.labelStyle = "icon-code-red";
    } else if (artifact.getArtifactType().getName().equals("text/html")) {
      this.labelStyle = "icon-www";
    } else if (artifact.getArtifactType().getName().equals("text/plain")) {
      this.labelStyle = "icon-txt";
    } else if (artifact.getArtifactType().getName().equals("application/pdf")) {
      this.labelStyle = "icon-pdf";
    } else if (artifact.getArtifactType().getName().equals("application/json;charset=UTF-8")) {
      this.labelStyle = "icon-code-blue";
    } else if (artifact.getArtifactType().getName().equals("application/msword")) {
      this.labelStyle = "icon-doc";
    } else if (artifact.getArtifactType().getName().equals("application/powerpoint")) {
      this.labelStyle = "icon-ppt";
    } else if (artifact.getArtifactType().getName().equals("application/excel")) {
      this.labelStyle = "icon-xls";
    } else if (artifact.getArtifactType().getName().equals("application/javascript")) {
      this.labelStyle = "icon-code-blue";
    } else {
      // Use white page as default icon for all other content types
      this.labelStyle = "icon-blank";
    }
  }

  public String getFile() {
    return file;
  }

  public String getLabelStyle() {
    return labelStyle;
  }

}
