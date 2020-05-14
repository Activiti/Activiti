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

package org.activiti.engine.impl.bpmn.parser.handler;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.ScriptTask;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**

 */
public class ScriptTaskParseHandler extends AbstractActivityBpmnParseHandler<ScriptTask> {

  private static final Logger logger = LoggerFactory.getLogger(ScriptTaskParseHandler.class);

  public Class<? extends BaseElement> getHandledType() {
    return ScriptTask.class;
  }

  protected void executeParse(BpmnParse bpmnParse, ScriptTask scriptTask) {

    if (StringUtils.isEmpty(scriptTask.getScript())) {
      logger.warn("No script provided for scriptTask " + scriptTask.getId());
    }

    scriptTask.setBehavior(bpmnParse.getActivityBehaviorFactory().createScriptTaskActivityBehavior(scriptTask));

  }

}
