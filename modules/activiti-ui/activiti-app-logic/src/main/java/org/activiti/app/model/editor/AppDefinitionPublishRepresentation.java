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
package org.activiti.app.model.editor;

import org.activiti.app.model.common.AbstractRepresentation;

public class AppDefinitionPublishRepresentation extends AbstractRepresentation {

  protected String comment;
  protected Boolean force;

  public AppDefinitionPublishRepresentation() {

  }

  public AppDefinitionPublishRepresentation(String comment, Boolean force) {
    this.comment = comment;
    this.force = force;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public Boolean getForce() {
    return force;
  }

  public void setForce(Boolean force) {
    this.force = force;
  }

}
