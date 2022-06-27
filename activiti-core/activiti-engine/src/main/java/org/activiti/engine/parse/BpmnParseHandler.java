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

package org.activiti.engine.parse;

import java.util.Collection;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;

/**
 * Allows to hook into the parsing of one or more elements during the parsing of a BPMN 2.0 process. For more details, see the userguide section on bpmn parse handlers.
 *
 * Instances of this class can be injected into the {@link ProcessEngineConfigurationImpl}. The handler will then be called whenever a BPMN 2.0 element is parsed that matches the types returned by the
 * {@link #getHandledTypes()} method.
 *
 * @see AbstractBpmnParseHandler
 *
 */
@Internal
public interface BpmnParseHandler {

  /**
   * The types for which this handler must be called during process parsing.
   */
  Collection<Class<? extends BaseElement>> getHandledTypes();

  /**
   * The actual delegation method. The parser will calls this method on a match with the {@link #getHandledTypes()} return value.
   *
   * @param bpmnParse
   *          The {@link BpmnParse} instance that acts as container for all things produced during the parsing.
   */
  void parse(BpmnParse bpmnParse, BaseElement element);

}
