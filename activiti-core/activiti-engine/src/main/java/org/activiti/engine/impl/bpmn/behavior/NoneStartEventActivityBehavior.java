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

package org.activiti.engine.impl.bpmn.behavior;

/**
 * implementation of the 'none start event': a start event that has no specific trigger but the programmatic one (processService.startProcessInstanceXXX()).
 *
 *

 */
public class NoneStartEventActivityBehavior extends FlowNodeActivityBehavior {

  private static final long serialVersionUID = 1L;

  // Nothing to see here.
  // The default behaviour of the BpmnActivity is exactly what
  // a none start event should be doing.

}
