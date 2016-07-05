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

import java.io.Serializable;

import org.activiti.form.engine.ActivitiFormIllegalArgumentException;
import org.activiti.form.engine.impl.interceptor.Command;
import org.activiti.form.engine.impl.interceptor.CommandContext;
import org.activiti.form.engine.impl.util.FormUtil;
import org.activiti.form.model.FormDefinition;

/**
 * @author Joram Barrez
 */
public class GetFormDefinitionCmd implements Command<FormDefinition>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String formId;

  public GetFormDefinitionCmd(String formId) {
    this.formId = formId;
  }

  public FormDefinition execute(CommandContext commandContext) {
    if (formId == null) {
      throw new ActivitiFormIllegalArgumentException("formId is null");
    }

    return FormUtil.getFormDefinition(formId);
  }
}