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
package org.activiti.engine;

/**
 * Runtime exception indicating the requested class was not found or an error occurred while loading the class.
 */
public class ActivitiClassLoadingException extends ActivitiException {

  private static final long serialVersionUID = 1L;
  protected String className;

  public ActivitiClassLoadingException(String className, Throwable cause) {
    super(getExceptionMessageMessage(className, cause), cause);
    this.className = className;
  }

  /**
   * Returns the name of the class this exception is related to.
   */
  public String getClassName() {
    return className;
  }

  private static String getExceptionMessageMessage(String className, Throwable cause) {
    if (cause instanceof ClassNotFoundException) {
      return "Class not found: " + className;
    } else {
      return "Could not load class: " + className;
    }
  }

}
