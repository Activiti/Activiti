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

import java.util.List;

import org.activiti.cycle.RepositoryFolder;

/**
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class TreeFolderDto extends TreeNodeDto {

  private final String folder = String.valueOf(Boolean.TRUE);
  private List<TreeNodeDto> children;

  public TreeFolderDto(RepositoryFolder folder) {
    super(folder);
    this.expanded = String.valueOf(Boolean.FALSE);
  }

  public String getFolder() {
    return folder;
  }

  public List<TreeNodeDto> getChildren() {
    return children;
  }

  public void setChildren(List<TreeNodeDto> children) {
    this.children = children;
  }

}
