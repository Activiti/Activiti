/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.bpmn.model.parse;

import org.activiti.bpmn.model.BaseElement;

public class Warning {

  protected String warningMessage;
  protected String resource;
  protected int line;
  protected int column;

  public Warning(String warningMessage, String localName, int lineNumber, int columnNumber) {
    this.warningMessage = warningMessage;
    this.resource = localName;
    this.line = lineNumber;
    this.column = columnNumber;
  }

  public Warning(String warningMessage, BaseElement element) {
    this.warningMessage = warningMessage;
    this.resource = element.getId();
    line = element.getXmlRowNumber();
    column = element.getXmlColumnNumber();
  }

  public String toString() {
    return warningMessage + (resource != null ? " | " + resource : "") + " | line " + line + " | column " + column;
  }
}
