/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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


package org.activiti.engine.impl.bpmn.parser;

import java.io.Serializable;
import java.util.Comparator;


public class ErrorEventDefinition implements Serializable {

  public static Comparator<ErrorEventDefinition> comparator = new Comparator<ErrorEventDefinition>() {
    public int compare(ErrorEventDefinition o1, ErrorEventDefinition o2) {
      return o2.getPrecedence().compareTo(o1.getPrecedence());
    }
  };

  private static final long serialVersionUID = 1L;

  protected final String handlerActivityId;
  protected String errorCode;
  protected Integer precedence = 0;

  public ErrorEventDefinition(String handlerActivityId) {
    this.handlerActivityId = handlerActivityId;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  public String getHandlerActivityId() {
    return handlerActivityId;
  }

  public Integer getPrecedence() {
    // handlers with error code take precedence over catchall-handlers
    return precedence + (errorCode != null ? 1 : 0);
  }

  public void setPrecedence(Integer precedence) {
    this.precedence = precedence;
  }

  public boolean catches(String errorCode) {
    return errorCode == null || this.errorCode == null || this.errorCode.equals(errorCode);
  }

}
