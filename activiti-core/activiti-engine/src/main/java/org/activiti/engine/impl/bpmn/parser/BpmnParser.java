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

package org.activiti.engine.impl.bpmn.parser;

import org.activiti.engine.impl.bpmn.parser.factory.ActivityBehaviorFactory;
import org.activiti.engine.impl.bpmn.parser.factory.ListenerFactory;
import org.activiti.engine.impl.cfg.BpmnParseFactory;

/**
 * Parser for BPMN 2.0 process models.
 *
 * There is only one instance of this parser in the process engine. This {@link BpmnParser} creates {@link BpmnParse} instances that can be used to actually parse the BPMN 2.0 XML process definitions.
 *


 */
public class BpmnParser {

  /**
   * The namepace of the BPMN 2.0 diagram interchange elements.
   */
  public static final String BPMN_DI_NS = "http://www.omg.org/spec/BPMN/20100524/DI";

  /**
   * The namespace of the BPMN 2.0 diagram common elements.
   */
  public static final String BPMN_DC_NS = "http://www.omg.org/spec/DD/20100524/DC";

  /**
   * The namespace of the generic OMG DI elements (don't ask me why they didn't use the BPMN_DI_NS ...)
   */
  public static final String OMG_DI_NS = "http://www.omg.org/spec/DD/20100524/DI";

  protected ActivityBehaviorFactory activityBehaviorFactory;
  protected ListenerFactory listenerFactory;
  protected BpmnParseFactory bpmnParseFactory;
  protected BpmnParseHandlers bpmnParserHandlers;

  /**
   * Creates a new {@link BpmnParse} instance that can be used to parse only one BPMN 2.0 process definition.
   */
  public BpmnParse createParse() {
    return bpmnParseFactory.createBpmnParse(this);
  }

  public ActivityBehaviorFactory getActivityBehaviorFactory() {
    return activityBehaviorFactory;
  }

  public void setActivityBehaviorFactory(ActivityBehaviorFactory activityBehaviorFactory) {
    this.activityBehaviorFactory = activityBehaviorFactory;
  }

  public ListenerFactory getListenerFactory() {
    return listenerFactory;
  }

  public void setListenerFactory(ListenerFactory listenerFactory) {
    this.listenerFactory = listenerFactory;
  }

  public BpmnParseFactory getBpmnParseFactory() {
    return bpmnParseFactory;
  }

  public void setBpmnParseFactory(BpmnParseFactory bpmnParseFactory) {
    this.bpmnParseFactory = bpmnParseFactory;
  }

  public BpmnParseHandlers getBpmnParserHandlers() {
    return bpmnParserHandlers;
  }

  public void setBpmnParserHandlers(BpmnParseHandlers bpmnParserHandlers) {
    this.bpmnParserHandlers = bpmnParserHandlers;
  }
}
