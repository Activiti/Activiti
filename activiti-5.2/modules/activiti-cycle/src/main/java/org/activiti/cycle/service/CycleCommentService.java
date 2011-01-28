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

import java.util.Date;
import java.util.List;

import org.activiti.cycle.RepositoryNodeComment;

public interface CycleCommentService {

  public void insertComment(String connectorId, String nodeId, String elementId, String content, String author, Date date, String answeredCommentId);

  public void deleteComment(String id);

  public List<RepositoryNodeComment> getCommentsForNode(String connectorId, String artifactId);

}
