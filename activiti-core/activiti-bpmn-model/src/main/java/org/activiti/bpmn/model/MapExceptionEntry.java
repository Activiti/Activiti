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
package org.activiti.bpmn.model;

public class MapExceptionEntry {

  String errorCode;
  String className;
  boolean andChildren;

  public MapExceptionEntry(String errorCode, String className, boolean andChildren) {

    this.errorCode = errorCode;
    this.className = className;
    this.andChildren = andChildren;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public boolean isAndChildren() {
    return andChildren;
  }

  public void setAndChildren(boolean andChildren) {
    this.andChildren = andChildren;
  }

}
