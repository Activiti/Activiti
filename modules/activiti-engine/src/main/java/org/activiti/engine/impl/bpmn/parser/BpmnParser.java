/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.bpmn.parser;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.cfg.BpmnParseFactory;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.util.xml.Parser;


/**
 * Parser for BPMN 2.0 process models.
 * 
 * There is only one instance of this parser in the process engine.
 * This {@link Parser} creates {@link BpmnParse} instances that 
 * can be used to actually parse the BPMN 2.0 XML process definitions.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class BpmnParser extends Parser {
  
  /**
   * The BPMN 2.0 namespace
   */
  public static final String BPMN20_NS = "http://www.omg.org/spec/BPMN/20100524/MODEL";
  
  /**
   * The location of the BPMN 2.0 XML schema.
   */
  public static final String BPMN_20_SCHEMA_LOCATION = "org/activiti/impl/bpmn/parser/BPMN20.xsd";

  /**
   * The namespace of the Activiti custom BPMN extensions.
   */
  public static final String ACTIVITI_BPMN_EXTENSIONS_NS = "http://activiti.org/bpmn";
  
  /**
   * The namepace of the BPMN 2.0 diagram interchange elements.
   */
  public static final String BPMN_DI_NS = "http://www.omg.org/spec/BPMN/20100524/DI";
  
  /**
   * The namespace of the BPMN 2.0 diagram common elements.
   */
  public static final String BPMN_DC_NS = "http://www.omg.org/spec/DD/20100524/DC";
  
  /**
   * The namespace of the generic OMG DI elements (don't ask me why they didnt use the BPMN_DI_NS ...)
   */
  public static final String OMG_DI_NS = "http://www.omg.org/spec/DD/20100524/DI";

  /**
   * The Schema-Instance namespace.
   */
  public static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";

  protected ExpressionManager expressionManager;
  protected List<BpmnParseListener> parseListeners = new ArrayList<BpmnParseListener>();
  
  protected BpmnParseFactory bpmnParseFactory;
  
  public BpmnParser(ExpressionManager expressionManager, BpmnParseFactory bpmnParseFactory) {
    this.expressionManager = expressionManager;
    this.bpmnParseFactory = bpmnParseFactory;
  }
  
  /**
   * Creates a new {@link BpmnParse} instance that can be used
   * to parse only one BPMN 2.0 process definition.
   */
  public BpmnParse createParse() {
    return bpmnParseFactory.createBpmnParse(this);
  }

  public ExpressionManager getExpressionManager() {
    return expressionManager;
  }

  public void setExpressionManager(ExpressionManager expressionManager) {
    this.expressionManager = expressionManager;
  }

  public List<BpmnParseListener> getParseListeners() {
    return parseListeners;
  }
  
  public void setParseListeners(List<BpmnParseListener> parseListeners) {
    this.parseListeners = parseListeners;
  }
}
