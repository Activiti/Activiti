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
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.impl.processsolution.artifacttype.ProcessSolutionHomeArtifactType;

/**
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class TreeLeafDto extends TreeNodeDto {

  private final String file = String.valueOf(Boolean.TRUE);
  private String labelStyle;

  public TreeLeafDto() {
  }

  public TreeLeafDto(RepositoryArtifact artifact) {
    super(artifact);
    this.expanded = String.valueOf(Boolean.TRUE);
    setArtifactType(artifact.getArtifactType());
  }

  public String getFile() {
    return file;
  }

  public String getLabelStyle() {
    return labelStyle;
  }
  public void setArtifactType(RepositoryArtifactType type) {
    if (type instanceof ProcessSolutionHomeArtifactType) {
      this.labelStyle = "icon-home";
    } else if (type.getName().equals("image/png") || type.getName().equals("image/gif") || type.getName().equals("image/jpeg")) {
      this.labelStyle = "icon-img";
    } else if (type.getName().equals("application/xml")) {
      this.labelStyle = "icon-code-red";
    } else if (type.getName().equals("text/html")) {
      this.labelStyle = "icon-www";
    } else if (type.getName().equals("text/plain")) {
      this.labelStyle = "icon-txt";
    } else if (type.getName().equals("application/pdf")) {
      this.labelStyle = "icon-pdf";
    } else if (type.getName().equals("application/json;charset=UTF-8")) {
      this.labelStyle = "icon-code-blue";
    } else if (type.getName().equals("application/msword")) {
      this.labelStyle = "icon-doc";
    } else if (type.getName().equals("application/powerpoint")) {
      this.labelStyle = "icon-ppt";
    } else if (type.getName().equals("application/excel")) {
      this.labelStyle = "icon-xls";
    } else if (type.getName().equals("application/javascript")) {
      this.labelStyle = "icon-code-blue";
    } else {
      // Use white page as default icon for all other content types
      this.labelStyle = "icon-blank";
    }

  }
}
