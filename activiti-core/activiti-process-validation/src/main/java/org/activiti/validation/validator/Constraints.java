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

package org.activiti.validation.validator;


public class Constraints {

  /**
   * Max length database field ACT_RE_PROCDEF.CATEGORY
   */
  public static final int BPMN_MODEL_TARGET_NAMESPACE_MAX_LENGTH = 255;

  /**
   * Max length database field ACT_RE_PROCDEF.KEY
   */
  public static final int PROCESS_DEFINITION_ID_MAX_LENGTH = 255;

  /**
   * Max length database field ACT_RE_PROCDEF.NAME
   */
  public static final int PROCESS_DEFINITION_NAME_MAX_LENGTH = 255;

  /**
   * Max length of database field ACT_RE_PROCDEF.DESCRIPTION
   */
  public static final int PROCESS_DEFINITION_DOCUMENTATION_MAX_LENGTH = 2000;

}
