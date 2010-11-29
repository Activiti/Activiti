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
package org.activiti.cycle.service;

import java.util.List;

import org.activiti.cycle.CycleTagContent;
import org.activiti.cycle.RepositoryNodeTag;

/**
 * Cycle service used for managing tags.
 * <p />
 * Get an instance of this service by {@link CycleService#getCycleTagService()}
 * 
 * @see CycleService
 */
public interface CycleTagService {

  /**
   * add tag for the given node id and specify an alias which can be used in the
   * GUI later on when showing the tag to the user
   */
  public void addTag(String connectorId, String nodeId, String tagName, String alias);

  /**
   * delete the tag
   */
  public void deleteTag(String connectorId, String nodeId, String tagName);

  public List<RepositoryNodeTag> getRepositoryNodeTags(String connectorId, String nodeId);

  public List<CycleTagContent> getRootTags();

  /**
   * get all tag names matching the given pattern. This can be used to find
   * already used tags to resuse them
   */
  public List<String> getSimiliarTagNames(String tagNamePattern);

  /**
   * get all available tags for the system in order to show them in the GUI (as
   * folder, tag cloud, ...)
   */
  public CycleTagContent getTagContent(String name);

  public List<String> getTags(String connectorId, String nodeId);

  /**
   * sets provided tags to the given artifact, this means it should reset the
   * previous tags for that artifact!
   * 
   * Additionally it does some magic for you:
   * <ul>
   * <li>checks for every tag whether it is empty (doesn't create it if that is
   * the case)</li>
   * <li>checks whether the tag already exists, CycleService should worry about
   * duplicate exceptions etc.</li>
   * </ul>
   */
  public void setTags(String connectorId, String nodeId, List<String> tags);

}
