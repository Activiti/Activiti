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
package org.activiti.engine.impl.util.json;

/**
 * The JSONException is thrown by the JSON.org classes when things are amiss.
 *

 * @version 2008-09-18
 */
public class JSONException extends RuntimeException {
  /**
	 *
	 */
  private static final long serialVersionUID = 0;
  private Throwable cause;

  /**
   * Constructs a JSONException with an explanatory message.
   *
   * @param message
   *          Detail about the reason for the exception.
   */
  public JSONException(String message) {
    super(message);
  }

  public JSONException(Throwable t) {
    super(t.getMessage());
    this.cause = t;
  }

  public Throwable getCause() {
    return this.cause;
  }
}
