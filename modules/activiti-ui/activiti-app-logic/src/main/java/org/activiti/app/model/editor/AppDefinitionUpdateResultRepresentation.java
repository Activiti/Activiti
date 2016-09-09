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

public class AppDefinitionUpdateResultRepresentation extends AbstractRepresentation {

  public static final int CUSTOM_STENCIL_ITEM = 1;
  public static final int MODEL_VALIDATION_ERRORS = 2;

  protected AppDefinitionRepresentation appDefinition;
  protected String message;
  protected String messageKey;
  protected boolean error;
  protected int errorType;
  protected String errorDescription;

  public AppDefinitionRepresentation getAppDefinition() {
    return appDefinition;
  }

  public void setAppDefinition(AppDefinitionRepresentation appDefinition) {
    this.appDefinition = appDefinition;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getMessageKey() {
    return messageKey;
  }

  public void setMessageKey(String messageKey) {
    this.messageKey = messageKey;
  }

  public boolean isError() {
    return error;
  }

  public void setError(boolean error) {
    this.error = error;
  }

  public int getErrorType() {
    return errorType;
  }

  public void setErrorType(int errorType) {
    this.errorType = errorType;
  }

  public String getErrorDescription() {
    return errorDescription;
  }

  public void setErrorDescription(String errorDescription) {
    this.errorDescription = errorDescription;
  }
}
