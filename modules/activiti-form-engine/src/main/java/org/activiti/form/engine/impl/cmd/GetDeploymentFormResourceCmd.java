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

package org.activiti.form.engine.impl.cmd;

import java.io.InputStream;
import java.io.Serializable;

import org.activiti.form.engine.ActivitiFormIllegalArgumentException;
import org.activiti.form.engine.impl.interceptor.Command;
import org.activiti.form.engine.impl.interceptor.CommandContext;
import org.activiti.form.engine.impl.persistence.entity.FormEntity;

/**
 * Gives access to a deployed form model, e.g., a Form JSON file, through a stream of bytes.
 * 
 * @author Tijs Rademakers
 */
public class GetDeploymentFormResourceCmd implements Command<InputStream>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String formId;

  public GetDeploymentFormResourceCmd(String formId) {
    if (formId == null || formId.length() < 1) {
      throw new ActivitiFormIllegalArgumentException("The form id is mandatory, but '" + formId + "' has been provided.");
    }
    this.formId = formId;
  }

  public InputStream execute(CommandContext commandContext) {
    FormEntity form = commandContext.getFormEngineConfiguration().getDeploymentManager().findDeployedFormById(formId);
    String deploymentId = form.getDeploymentId();
    String resourceName = form.getResourceName();
    InputStream formStream = new GetDeploymentResourceCmd(deploymentId, resourceName).execute(commandContext);
    return formStream;
  }

}
