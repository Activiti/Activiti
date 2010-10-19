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
package org.activiti.cycle;

import java.util.List;

import org.activiti.cycle.impl.conf.ConfigurationContainer;

/**
 * This is the central entry point for Activiti Cycle. The service provides the
 * possibility to store and load user configurations (which then contains
 * {@link RepositoryConnector}s) and to do global actions not tied to a single
 * repository like Tags, Links and so on...
 */
public interface CycleService {
  
  public ConfigurationContainer getConfiguration(String name);
  
  public void saveConfiguration(ConfigurationContainer container);

  /**
   * adds default {@link Artifact} for the supplied artifact ids. If you need
   * more specific links please use the "addLink" method
   */
  public void addArtifactLink(String sourceArtifactId, String targetArtifactId);

  /**
   * add given link to Cycle
   */
  public void addLink(RepositoryArtifactLink link); 
  
  public List<RepositoryArtifactLink> getArtifactLinks(String sourceArtifactId);
  public List<RepositoryArtifactLink> getArtifactLinks(String sourceArtifactId, Long sourceRevision);
  public List<RepositoryArtifactLink> getArtifactLinks(String sourceArtifactId, String type);
  public List<RepositoryArtifactLink> getArtifactLinks(String sourceArtifactId, Long sourceRevision, String type);
 
  public void deleteLink(long linkId);

  /**
   * add tag for the given node id. Tags are identified by their string names
   */
  public void addTag(String nodeId, String tagName);

  /**
   * add tag for the given node id and specify an alias which can be used in the
   * GUI later on when showing the tag to the user
   */
  public void addTag(String nodeId, String tagName, String alias);
  
  /**
   * delete the
   */
  public void deleteTag(String nodeId, String tagName);

  /**
   * returns all {@link CycleTag}s for the {@link RepositoryNode} with the given
   * id. Returns an empty list if not tags are available. Please note that
   * different alias for tag names lead to different {@link CycleTag} objects.
   */
  public List<CycleTag> getTags(String nodeId) throws RepositoryNodeNotFoundException;
  
  /**
   * get all available tags for the system in order to show them in the GUI (as
   * folder, tag cloud, ...)
   */
  public List<CycleTag> getAllTags(); 

  /**
   * get all available tags for the system in order to show them in the GUI (as
   * folder, tag cloud, ...) but ignore the alias settings, meaning the same tag
   * names with different alias are merged together (which normally make sense
   * for the top level GUI)
   */
  public List<CycleTag> getAllTagsIgnoreAlias(); 
   
}
