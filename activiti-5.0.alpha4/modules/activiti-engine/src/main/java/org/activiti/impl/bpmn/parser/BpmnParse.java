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
package org.activiti.impl.bpmn.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.ActivitiException;
import org.activiti.ProcessDefinition;
import org.activiti.impl.bpmn.BoundaryTimerEventActivity;
import org.activiti.impl.bpmn.BpmnInterface;
import org.activiti.impl.bpmn.CallActivityBehaviour;
import org.activiti.impl.bpmn.ExclusiveGatewayActivity;
import org.activiti.impl.bpmn.ManualTaskActivity;
import org.activiti.impl.bpmn.NoneEndEventActivity;
import org.activiti.impl.bpmn.NoneStartEventActivity;
import org.activiti.impl.bpmn.Operation;
import org.activiti.impl.bpmn.ParallelGatewayActivity;
import org.activiti.impl.bpmn.ReceiveTaskActivity;
import org.activiti.impl.bpmn.ScriptTaskActivity;
import org.activiti.impl.bpmn.ServiceInvocationActivityBehaviour;
import org.activiti.impl.bpmn.SubProcessActivity;
import org.activiti.impl.bpmn.TaskActivity;
import org.activiti.impl.bpmn.UserTaskActivity;
import org.activiti.impl.calendar.BusinessCalendar;
import org.activiti.impl.calendar.BusinessCalendarManager;
import org.activiti.impl.calendar.DurationBusinessCalendar;
import org.activiti.impl.definition.ActivityImpl;
import org.activiti.impl.definition.FormReference;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.definition.ScopeElementImpl;
import org.activiti.impl.definition.TransitionImpl;
import org.activiti.impl.el.ExpressionManager;
import org.activiti.impl.el.UelMethodExpressionCondition;
import org.activiti.impl.el.UelValueExpressionCondition;
import org.activiti.impl.job.TimerExecuteNestedActivityJobHandler;
import org.activiti.impl.persistence.PersistenceSession;
import org.activiti.impl.scripting.ScriptingEngines;
import org.activiti.impl.task.TaskDefinition;
import org.activiti.impl.timer.TimerDeclarationImpl;
import org.activiti.impl.variable.VariableDestroyWithExpression;
import org.activiti.impl.variable.VariableDestroyWithVariable;
import org.activiti.impl.variable.VariableInitializeWithExpression;
import org.activiti.impl.variable.VariableInitializeWithVariable;
import org.activiti.impl.xml.Element;
import org.activiti.impl.xml.Parse;
import org.activiti.impl.xml.Parser;
import org.activiti.pvm.ActivityBehavior;
import org.activiti.pvm.Condition;
import org.activiti.pvm.Listener;
import org.xml.sax.SAXParseException;

/**
 * Specific parsing representation created by the {@link BpmnParser} to parse
 * one BPMN 2.0 process XML file.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class BpmnParse extends Parse {

  private static final Logger LOG = Logger.getLogger(BpmnParse.class.getName());

  /**
   * The end result of the parsing: a list of process definition.
   */
  private final List<ProcessDefinitionImpl> processDefinitions = new ArrayList<ProcessDefinitionImpl>();

  /**
   * The class to instantiate during parsing where all the runtime data will be
   * stored.
   */
  protected Class< ? extends ProcessDefinition> processDefinitionClass = ProcessDefinitionImpl.class;

  /**
   * Map containing the BPMN 2.0 item definitions, stored during the first phase
   * of parsing since other elements can reference these item definitions.
   * 
   * Item definitions are defined outside the process definition(s), which means
   * that this map doesn't need to be re-initialized for each new process
   * definition.
   */
  protected Map<String, Element> itemDefinitions = new HashMap<String, Element>();

  /**
   * Map containing the the {@link BpmnInterface}s defined in the XML file. The
   * key is the id of the interface.
   * 
   * Interfaces are defined outside the process definition(s), which means that
   * this map doesn't need to be re-initialized for each new process definition.
   */
  protected Map<String, BpmnInterface> bpmnInterfaces = new HashMap<String, BpmnInterface>();

  /**
   * Map containing the {@link Operation}s defined in the XML file. The key is
   * the id of the operations.
   * 
   * Operations are defined outside the process definition(s), which means that
   * this map doesn't need to be re-initialized for each new process definition.
   */
  protected Map<String, Operation> operations = new HashMap<String, Operation>();

  protected ExpressionManager expressionManager;

  protected ScriptingEngines scriptingEngines;

  protected BusinessCalendarManager businessCalendarManager;
  
  /**
   * Constructor to be called by the {@link BpmnParser}.
   * 
   * Note the package modifier here: only the {@link BpmnParser} is allowed to
   * create instances.
   */
  BpmnParse(Parser parser, ExpressionManager expressionManager, ScriptingEngines scriptingEngines,
          BusinessCalendarManager businessCalendarManager) {
    super(parser);
    this.expressionManager = expressionManager;
    this.scriptingEngines = scriptingEngines;
    this.businessCalendarManager = businessCalendarManager;
    setSchemaResource(BpmnParser.SCHEMA_RESOURCE);
  }

  @Override
  public BpmnParse execute() {
    try {
      super.execute(); // schema validation
    } catch (ActivitiException e) {
      // Fall back to beta 1 XSD (see ACT-52)
      if (e.getMessage().toLowerCase().contains("cannot find the declaration of element 'definitions'")) {
        try {
          streamSource.getInputStream().reset();
          setSchemaResource(BpmnParser.BETA_SCHEMA_RESOURCE);
          super.execute();
        } catch (IOException ioe) {
          throw e;
        }
      } else {
        throw e;
      }
    }

    // Item definitions and interfaces/operations are not part of any process definition
    // They need to be parsed before the process definitions since they will refer to them
    parseItemDefinitions(rootElement);
    parseInterfaces(rootElement);

    parseProcessDefinitions(rootElement);
    return this;
  }

  /**
   * Parses the itemDefinitions of the given definitions file. Item definitions
   * are not contained within a process element, but they can be referenced from
   * inner process elements.
   * 
   * @param definitionsElement
   *          The root element of the XML file.
   */
  public void parseItemDefinitions(Element definitionsElement) {
    for (Element itemDefinitionElement : definitionsElement.elements("itemDefinition")) {
      itemDefinitions.put(itemDefinitionElement.attribute("id"), itemDefinitionElement);
    }
  }

  /**
   * Parses the interfaces and operations defined withing the root element.
   * 
   * @param definitionsElement
   *          The root element of the XML file/
   */
  public void parseInterfaces(Element definitionsElement) {
    for (Element interfaceElement : definitionsElement.elements("interface")) {

      // Create the interface
      String id = interfaceElement.attribute("id");
      String name = interfaceElement.attribute("name");
      BpmnInterface bpmnInterface = new BpmnInterface(id, name);

      // Handle all its operations
      for (Element operationElement : interfaceElement.elements("operation")) {
        Operation operation = parseOperation(operationElement, bpmnInterface);
        bpmnInterface.addOperation(operation);
      }

      bpmnInterfaces.put(id, bpmnInterface);
    }
  }

  public Operation parseOperation(Element operationElement, BpmnInterface bpmnInterface) {
    String id = operationElement.attribute("id");
    String name = operationElement.attribute("name");
    Operation operation = new Operation(id, name, bpmnInterface);

    operations.put(id, operation);
    return operation;
  }

  /**
   * Parses all the process definitions defined within the 'definitions' root
   * element.
   * 
   * @param definitionsElement
   *          The root element of the XML file.
   */
  public void parseProcessDefinitions(Element definitionsElement) {
    // TODO: parse specific definitions data (id, imports, etc)
    for (Element processElement : definitionsElement.elements("process")) {
      processDefinitions.add(parseProcess(processElement));
    }
  }

  /**
   * Parses one process (ie anything inside a &lt;process&gt; element).
   * 
   * @param processElement
   *          The 'process' element.
   * @return The parsed version of the XML: a {@link ProcessDefinitionImpl}
   *         object.
   */
  public ProcessDefinitionImpl parseProcess(Element processElement) {
    ProcessDefinitionImpl processDefinition = null;
    try {
      processDefinition = (ProcessDefinitionImpl) processDefinitionClass.newInstance();
    } catch (Exception e) {
      throw new ActivitiException("couldn't instantiate process definition '" + processDefinitionClass + "'", e);
    }

    /*
     * Mapping object model - bpmn xml: processDefinition.id -> generated by
     * activiti engine processDefinition.key -> bpmn id (required)
     * processDefinition.name -> bpmn name (optional)
     */
    processDefinition.setKey(processElement.attribute("id"));
    processDefinition.setName(processElement.attribute("name"));

    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Parsing process " + processDefinition.getKey());
    }

    parseScope(processElement, processDefinition);
     
    return processDefinition;
  }
  
  /**
   * Parses a scope: a process, subprocess, etc.
   * 
   * Note that a process definition is a scope on itself.
   * 
   * @param scopeElement The XML element defining the scope
   * @param parentScope The scope that contains the nested scope. 
   */
  public void parseScope(Element scopeElement, ScopeElementImpl parentScope) {
    
    // Not yet supported on process level (PVM additions needed):
    // parseProperties(processElement);
    
    parseStartEvents(scopeElement, parentScope);
    parseActivities(scopeElement, parentScope);
    parseEndEvents(scopeElement, parentScope);
    parseBoundaryEvents(scopeElement, parentScope);
    parseSequenceFlow(scopeElement, parentScope);
  }

  /**
   * Parses the start events of a certain level in the process (process,
   * subprocess or another scope).
   * 
   * @param parentElement
   *          The 'parent' element that contains the start events (process,
   *          subprocess).
   * @param scopeElement
   *          The {@link ScopeElementImpl} to which the start events must be
   *          added.
   */
  public void parseStartEvents(Element parentElement, ScopeElementImpl scopeElement) {
    List<Element> startEventElements = parentElement.elements("startEvent");
    if (startEventElements.size() > 1) {
      throw new ActivitiException("Multiple start events are currently unsupported");
    } else if (startEventElements.size() > 0) {

      Element startEventElement = startEventElements.get(0);

      String id = startEventElement.attribute("id");
      String name = startEventElement.attribute("name");

      ActivityImpl activity = scopeElement.createActivity(id);
      activity.setName(name);
      scopeElement.setInitial(activity);

      String form = startEventElement.attributeNS(BpmnParser.BPMN_EXTENSIONS_NS, "form");
      String formLanguage = startEventElement.attributeNS(BpmnParser.BPMN_EXTENSIONS_NS, "form-language", ScriptingEngines.DEFAULT_SCRIPTING_LANGUAGE);
      if (form != null) {
        activity.setFormReference(new FormReference(form, formLanguage));
      }

      // Currently only none start events supported
      
      // TODO: a subprocess is only allowed to have a none start event 
      activity.setActivityBehavior(new NoneStartEventActivity());
    }
  }

  /**
   * Parses the activities of a certain level in the process (process,
   * subprocess or another scope).
   * 
   * @param parentElement
   *          The 'parent' element that contains the activities (process,
   *          subprocess).
   * @param scopeElement
   *          The {@link ScopeElementImpl} to which the activities must be
   *          added.
   */
  public void parseActivities(Element parentElement, ScopeElementImpl scopeElement) {
    for (Element activityElement : parentElement.elements()) {
      if (activityElement.getTagName().equals("exclusiveGateway")) {
        parseExclusiveGateway(activityElement, scopeElement);
      } else if (activityElement.getTagName().equals("parallelGateway")) {
        parseParallelGateway(activityElement, scopeElement);
      } else if (activityElement.getTagName().equals("scriptTask")) {
        parseScriptTask(activityElement, scopeElement);
      } else if (activityElement.getTagName().equals("serviceTask")) {
        parseServiceTask(activityElement, scopeElement);
      } else if (activityElement.getTagName().equals("task")) {
        parseTask(activityElement, scopeElement);
      } else if (activityElement.getTagName().equals("manualTask")) {
        parseManualTask(activityElement, scopeElement);
      } else if (activityElement.getTagName().equals("userTask")) {
        parseUserTask(activityElement, scopeElement);
      } else if (activityElement.getTagName().equals("receiveTask")) {
        parseReceiveTask(activityElement, scopeElement);
      } else if (activityElement.getTagName().equals("subProcess")) {
        parseSubProcess(activityElement, scopeElement);
      } else if (activityElement.getTagName().equals("callActivity")) {
        parseCallActivity(activityElement, scopeElement);
      }
    }
  }

  /**
   * Generic parsing method for most flow elements: parsing of the documentation
   * sub-element.
   */
  public String parseDocumentation(Element element) {
    Element docElement = element.element("documentation");
    if (docElement != null) {
      return docElement.getText().trim();
    }
    return null;
  }

  /**
   * Parses the generic information of an activity element (id, name), and
   * creates a new {@link ActivityImpl} on the given scope element.
   */
  public ActivityImpl parseAndCreateActivityOnScopeElement(Element activityElement, ScopeElementImpl scopeElement) {

    String id = activityElement.attribute("id");
    String name = activityElement.attribute("name");
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Parsing activity " + id);
    }
    ActivityImpl activity = scopeElement.createActivity(id);
    activity.setName(name);
    activity.setType(activityElement.getTagName());
    return activity;
  }

  /**
   * Parses an exclusive gateway declaration.
   */
  public void parseExclusiveGateway(Element exclusiveGwElement, ScopeElementImpl scopeElement) {
    ActivityImpl activity = parseAndCreateActivityOnScopeElement(exclusiveGwElement, scopeElement);
    activity.setActivityBehavior(new ExclusiveGatewayActivity());
  }

  /**
   * Parses a parallel gateway declaration.
   */
  public void parseParallelGateway(Element parallelGwElement, ScopeElementImpl scopeElement) {
    ActivityImpl activity = parseAndCreateActivityOnScopeElement(parallelGwElement, scopeElement);
    activity.setActivityBehavior(new ParallelGatewayActivity());
  }

  /**
   * Parses a scriptTask declaration.
   */
  public void parseScriptTask(Element scriptTaskElement, ScopeElementImpl scopeElement) {
    ActivityImpl activity = parseAndCreateActivityOnScopeElement(scriptTaskElement, scopeElement);
    String script = null;
    String language = null;

    Element scriptElement = scriptTaskElement.element("script");
    if (scriptElement != null) {
      script = scriptElement.getText();
      
      // ACT-52: BPMN 2.0 Beta compatibility
      language = scriptTaskElement.attribute("scriptLanguage");
      // end compatibility
      
      if (language == null) {
        language = scriptTaskElement.attribute("scriptFormat");
      }
      
      if (language == null) {
        language = ScriptingEngines.DEFAULT_SCRIPTING_LANGUAGE;
      }
    }
    activity.setActivityBehavior(new ScriptTaskActivity(scriptingEngines, script, language));
  }

  /**
   * Parses a serviceTask declaration.
   */
  public void parseServiceTask(Element serviceTaskElement, ScopeElementImpl scopeElement) {
    ActivityImpl activity = parseAndCreateActivityOnScopeElement(serviceTaskElement, scopeElement);

    String expression = serviceTaskElement.attributeNS(BpmnParser.BPMN_EXTENSIONS_NS, "class");
    if (expression != null && expression.trim().length() > 0) {
      activity.setActivityBehavior(new ServiceInvocationActivityBehaviour(expressionManager.createValueExpression(expression)));
    } else {
      throw new ActivitiException("java attribute is mandatory on serviceTask");
    }

    // OLD implementation with BPMN interfaces/operations/etc
    // String operationRef = serviceTaskElement.attribute("operationRef");
    // if (operationRef != null) {
    // Operation operation = getOperation(operationRef);
    // if (operation != null) {
    //        
    // BpmnInterface bpmnInterface = operation.getInterface();
    // String className = bpmnInterface.getName();
    // String method = operation.getName();
    // activity.setActivityBehavior(new JavaServiceTaskActivity(className));
    //        
    // } else {
    // throw new ActivitiException("Invalid operationReference on line " +
    // serviceTaskElement.getLine()
    // + ": operation '" + operationRef + "' not found");
    // }
    // } else {
    // throw new ActivitiException("Line " + serviceTaskElement.getLine()
    // + ": The attribute operationRef on a serviceTask is required");
    // }
  }

  /**
   * Parses a task with no specific type (behaves as passthrough).
   */
  public void parseTask(Element taskElement, ScopeElementImpl scopeElement) {
    ActivityImpl activity = parseAndCreateActivityOnScopeElement(taskElement, scopeElement);
    activity.setActivityBehavior(new TaskActivity());
  }

  /**
   * Parses a manual task.
   */
  public void parseManualTask(Element manualTaskElement, ScopeElementImpl scopeElement) {
    ActivityImpl activity = parseAndCreateActivityOnScopeElement(manualTaskElement, scopeElement);
    activity.setActivityBehavior(new ManualTaskActivity());
  }

  /**
   * Parses a receive task.
   */
  public void parseReceiveTask(Element receiveTaskElement, ScopeElementImpl scopeElement) {
    ActivityImpl activity = parseAndCreateActivityOnScopeElement(receiveTaskElement, scopeElement);
    activity.setActivityBehavior(new ReceiveTaskActivity());
  }

  /* userTask specific finals */

  protected static final String HUMAN_PERFORMER = "humanPerformer";
  protected static final String POTENTIAL_OWNER = "potentialOwner";

  protected static final String RESOURCE_ASSIGNMENT_EXPR = "resourceAssignmentExpression";
  protected static final String FORMAL_EXPRESSION = "formalExpression";

  protected static final String USER_PREFIX = "user(";
  protected static final String GROUP_PREFIX = "group(";

  protected static final String ASSIGNEE_EXTENSION = "assignee";
  protected static final String CANDIDATE_USERS_EXTENSION = "candidateUsers";
  protected static final String CANDIDATE_GROUPS_EXTENSION = "candidateGroups";

  /**
   * Parses a userTask declaration.
   */
  public void parseUserTask(Element userTaskElement, ScopeElementImpl scopeElement) {
    ActivityImpl activity = parseAndCreateActivityOnScopeElement(userTaskElement, scopeElement);
    UserTaskActivity userTaskActivity = new UserTaskActivity(expressionManager, parseTaskDefinition(userTaskElement));

    String form = userTaskElement.attributeNS(BpmnParser.BPMN_EXTENSIONS_NS, "form");
    String formLanguage = userTaskElement.attributeNS(BpmnParser.BPMN_EXTENSIONS_NS, "form-language", ScriptingEngines.DEFAULT_SCRIPTING_LANGUAGE);
    if (form != null) {
      activity.setFormReference(new FormReference(form, formLanguage));
    }

    activity.setActivityBehavior(userTaskActivity);

    // TODO: needs to be generalized for every activity.
    if (parseProperties(userTaskElement, activity)) {
      activity.setScope(true);
    }
  }

  public TaskDefinition parseTaskDefinition(Element taskElement) {
    TaskDefinition taskDefinition = new TaskDefinition();

    String name = taskElement.attribute("name");
    if (name != null) {
      taskDefinition.setName(name);
    }

    taskDefinition.setDescription(parseDocumentation(taskElement));
    parseHumanPerformer(taskElement, taskDefinition);
    parsePotentialOwner(taskElement, taskDefinition);

    // Activiti custom extension
    parseUserTaskCustomExtensions(taskElement, taskDefinition);

    return taskDefinition;
  }

  protected void parseName(Element taskElement, TaskDefinition taskDefinition) {

  }

  protected void parseHumanPerformer(Element taskElement, TaskDefinition taskDefinition) {
    List<Element> humanPerformerElements = taskElement.elements(HUMAN_PERFORMER);

    if (humanPerformerElements.size() > 1) {
      throw new ActivitiException("Invalid task definition: multiple " + HUMAN_PERFORMER + " sub elements defined for " + taskDefinition.getName());
    } else if (humanPerformerElements.size() == 1) {
      Element humanPerformerElement = humanPerformerElements.get(0);
      if (humanPerformerElement != null) {
        parseHumanPerformerResourceAssignment(humanPerformerElement, taskDefinition);
      }
    }

  }

  protected void parsePotentialOwner(Element taskElement, TaskDefinition taskDefinition) {
    List<Element> potentialOwnerElements = taskElement.elements(POTENTIAL_OWNER);
    for (Element potentialOwnerElement : potentialOwnerElements) {
      parsePotentialOwnerResourceAssignment(potentialOwnerElement, taskDefinition);
    }
  }

  protected void parseHumanPerformerResourceAssignment(Element performerElement, TaskDefinition taskDefinition) {
    Element raeElement = performerElement.element(RESOURCE_ASSIGNMENT_EXPR);
    if (raeElement != null) {
      Element feElement = raeElement.element(FORMAL_EXPRESSION);
      if (feElement != null) {
        taskDefinition.setAssignee(feElement.getText());
      }
    }
  }

  protected void parsePotentialOwnerResourceAssignment(Element performerElement, TaskDefinition taskDefinition) {
    Element raeElement = performerElement.element(RESOURCE_ASSIGNMENT_EXPR);
    if (raeElement != null) {
      Element feElement = raeElement.element(FORMAL_EXPRESSION);
      if (feElement != null) {
        String[] assignmentExpressions = splitCommaSeparatedExpression(feElement.getText());
        for (String assignmentExpression : assignmentExpressions) {
          assignmentExpression = assignmentExpression.trim();
          if (assignmentExpression.startsWith(USER_PREFIX)) {
            taskDefinition.addCandidateUserId(getAssignmentId(assignmentExpression, USER_PREFIX));
          } else if (assignmentExpression.startsWith(GROUP_PREFIX)) {
            taskDefinition.addCandidateGroupId(getAssignmentId(assignmentExpression, GROUP_PREFIX));
          } else { // default: given string is a goupId, as-is.
            taskDefinition.addCandidateGroupId(assignmentExpression);
          }
        }
      }
    }
  }

  protected String[] splitCommaSeparatedExpression(String expression) {
    if (expression == null) {
      throw new ActivitiException("Invalid: no content for " + FORMAL_EXPRESSION + " provided");
    }
    return expression.split(",");
  }

  protected String getAssignmentId(String expression, String prefix) {
    return expression.substring(prefix.length(), expression.length() - 1).trim();
  }

  protected void parseUserTaskCustomExtensions(Element taskElement, TaskDefinition taskDefinition) {

    // assignee
    String assignee = taskElement.attributeNS(BpmnParser.BPMN_EXTENSIONS_NS, ASSIGNEE_EXTENSION);
    if (assignee != null) {
      if (taskDefinition.getAssignee() == null) {
        taskDefinition.setAssignee(assignee);
      } else {
        throw new ActivitiException("Invalid usage: duplicate assignee declaration for task " + taskDefinition.getName());
      }
    }

    // Candidate users
    String candidateUsersString = taskElement.attributeNS(BpmnParser.BPMN_EXTENSIONS_NS, CANDIDATE_USERS_EXTENSION);
    if (candidateUsersString != null) {
      String[] candidateUsers = candidateUsersString.split(",");
      for (String candidateUser : candidateUsers) {
        taskDefinition.addCandidateUserId(candidateUser.trim());
      }
    }

    // Candidate groups
    String candidateGroupsString = taskElement.attributeNS(BpmnParser.BPMN_EXTENSIONS_NS, CANDIDATE_GROUPS_EXTENSION);
    if (candidateGroupsString != null) {
      String[] candidateGroups = candidateGroupsString.split(",");
      for (String candidateGroup : candidateGroups) {
        taskDefinition.addCandidateGroupId(candidateGroup.trim());
      }
    }
  }

  /**
   * Parses the end events of a certain level in the process (process,
   * subprocess or another scope).
   * 
   * @param parentElement
   *          The 'parent' element that contains the end events (process,
   *          subprocess).
   * @param scopeElement
   *          The {@link ScopeElementImpl} to which the end events must be
   *          added.
   */
  public void parseEndEvents(Element parentElement, ScopeElementImpl scopeElement) {
    for (Element endEventElement : parentElement.elements("endEvent")) {
      String id = endEventElement.attribute("id");
      String name = endEventElement.attribute("name");

      ActivityImpl activity = scopeElement.createActivity(id);
      activity.setName(name);

      // Only none end events are currently supported
      activity.setActivityBehavior(new NoneEndEventActivity());
    }
  }

  /**
   * Parses the boundary events of a certain 'level' (process, subprocess or
   * other scope).
   * 
   * Note that the boundary events are not parsed during the parsing of the bpmn
   * activities, since the semantics are different (boundaryEvent needs to be
   * added as nested activity to the reference activity on PVM level).
   * 
   * @param parentElement
   *          The 'parent' element that contains the activities (process,
   *          subprocess).
   * @param scopeElement
   *          The {@link ScopeElementImpl} to which the activities must be
   *          added.
   */
  public void parseBoundaryEvents(Element parentElement, ScopeElementImpl scopeElement) {
    for (Element boundaryEventElement : parentElement.elements("boundaryEvent")) {

      // The boundary event is attached to an activity, reference by the
      // 'attachedToRef' attribute
      String attachedToRef = boundaryEventElement.attribute("attachedToRef");
      if (attachedToRef == null || attachedToRef.equals("")) {
        throw new ActivitiException("AttachedToRef is required when using a timerEventDefinition");
      }

      // Representation structure-wise is a nested activity in the activity to
      // which its attached
      String id = boundaryEventElement.attribute("id");
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Parsing boundary event " + id);
      }

      ActivityImpl parentActivity = scopeElement.findActivity(attachedToRef);
      if (parentActivity == null) {
        throw new ActivitiException("Invalid reference in boundary event: " + attachedToRef
              + " Make sure that the referenced activity is defined in the same scope as the boundary event");
      }
      ActivityImpl nestedActivity = parentActivity.createActivity(id);
      nestedActivity.setName(boundaryEventElement.attribute("name"));

      String cancelActivity = boundaryEventElement.attribute("cancelActivity", "true");
      boolean interrupting = cancelActivity.equals("true") ? true : false;

      // Depending on the sub-element definition, the correct activityBehavior
      // parsing is selected
      Element timerEventDefinition = boundaryEventElement.element("timerEventDefinition");
      if (timerEventDefinition != null) {
        parseBoundaryTimerEventDefinition(timerEventDefinition, interrupting, nestedActivity);
      } else {
        throw new ActivitiException("Unsupported boundary event type");
      }
    }
  }

  /**
   * Parses a boundary timer event. The end-result will be that the given nested
   * activity will get the appropriate {@link ActivityBehavior}.
   * 
   * @param timerEventDefinition
   *          The XML element corresponding with the timer event details
   * @param interrupting
   *          Indicates whether this timer is interrupting.
   * @param timerActivity
   *          The activity which maps to the structure of the timer event on the
   *          boundary of another activity. Note that this is NOT the activity
   *          onto which the boundary event is attached, but a nested activity
   *          inside this activity, specifically created for this event.
   */
  public void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting, ActivityImpl timerActivity) {
    BoundaryTimerEventActivity boundaryTimerEventActivity = new BoundaryTimerEventActivity();
    boundaryTimerEventActivity.setInterrupting(interrupting);

    // TimeDate

    // TimeCycle

    // TimeDuration
    Element timeDuration = timerEventDefinition.element("timeDuration");
    String timeDurationText = null;
    if (timeDuration != null) {
      timeDurationText = timeDuration.getText();
    }

    BusinessCalendar businessCalendar = businessCalendarManager.getBusinessCalendar(DurationBusinessCalendar.NAME);

    // TODO: check if any is given
    TimerDeclarationImpl timerDeclaration = timerActivity.getParent().createTimerDeclaration(businessCalendar, timeDurationText,
            TimerExecuteNestedActivityJobHandler.TYPE);
    timerDeclaration.setJobHandlerConfiguration(timerActivity.getId());
    timerActivity.setActivityBehavior(boundaryTimerEventActivity);
  }
  
  /**
   * Parses a subprocess (formely known as an embedded subprocess): a subprocess
   * defined withing another process definition.
   * 
   * @param subProcessElement The XML element corresponding with the subprocess definition
   * @param scopeElement The current scope on which the subprocess is defined.
   */
  public void parseSubProcess(Element subProcessElement, ScopeElementImpl scopeElement) {
    ActivityImpl activity = parseAndCreateActivityOnScopeElement(subProcessElement, scopeElement);
    activity.setScope(true);
    activity.setActivityBehavior(new SubProcessActivity());
    parseScope(subProcessElement, activity);
  }
  
  /**
   * Parses a call activity (currenly only supporting calling subprocesses).
   * 
   * @param callActivityElement The XML element defining the call activity
   * @param scopeElement The current scope on which the call activity is defined.
   */
  public void parseCallActivity(Element callActivityElement, ScopeElementImpl scopeElement) {
    ActivityImpl activity = parseAndCreateActivityOnScopeElement(callActivityElement, scopeElement);
    String calledElement = callActivityElement.attribute("calledElement");
    if (calledElement == null) {
      throw new ActivitiException("Missing attribute 'calledElement' on callActivity (line " +
              + callActivityElement.getLine() + ")");
    }
    activity.setActivityBehavior(new CallActivityBehaviour(calledElement));
  }

  /**
   * Parses the properties of an element (if any) that can contain properties
   * (processes, activities, etc.)
   * 
   * Returns true if property subelemens are found.
   * 
   * @param element
   *          The element that can contain properties.
   * @param activity
   *          The activity where the property declaration is done.
   */
  public boolean parseProperties(Element element, ActivityImpl activity) {
    List<Element> propertyElements = element.elements("property");
    for (Element propertyElement : propertyElements) {
      parseProperty(propertyElement, activity);
    }
    return !propertyElements.isEmpty();
  }

  /**
   * Parses one property definition.
   * 
   * @param propertyElement
   *          The 'property' element that defines how a property looks like and
   *          is handled.
   */
  public void parseProperty(Element propertyElement, ActivityImpl activity) {
    String id = propertyElement.attribute("id");
    String name = propertyElement.attribute("name");

    // If name isn't given, use the id as name
    if (name == null) {
      if (id == null) {
        throw new ActivitiException("Invalid property usage on line " + propertyElement.getLine() + ": no id or name specified.");
      } else {
        name = id;
      }
    }

    String itemSubjectRef = propertyElement.attribute("itemSubjectRef");
    String type = null;
    if (itemSubjectRef != null) {
      Element itemDefinitionRef = itemDefinitions.get(itemSubjectRef);
      if (itemDefinitionRef != null) {
        type = itemDefinitionRef.attribute("structureRef");
      } else {
        throw new ActivitiException("Invalid itemDefinition reference: " + itemSubjectRef + " not found");
      }
    }

    parsePropertyCustomExtensions(activity, propertyElement, name, type);
  }

  /**
   * Parses the custom extensions for properties.
   * 
   * @param activity
   *          The activity where the property declaration is done.
   * @param propertyElement
   *          The 'property' element defining the property.
   * @param propertyName
   *          The name of the property.
   * @param propertyType
   *          The type of the property.
   */
  public void parsePropertyCustomExtensions(ActivityImpl activity, Element propertyElement, String propertyName, String propertyType) {

    if (propertyType == null) {
      String type = propertyElement.attribute("activiti:type");
      propertyType = type != null ? type : "string"; // default is string
    }

    activity.createVariableDeclaration(propertyName, propertyType);

    String src = propertyElement.attributeNS(BpmnParser.BPMN_EXTENSIONS_NS, "src");
    if (src != null) {
      activity.addEventListener(Listener.EVENTNAME_START, new VariableInitializeWithVariable(src, propertyName));
    }

    String srcExpr = propertyElement.attributeNS(BpmnParser.BPMN_EXTENSIONS_NS, "srcExpr");
    if (srcExpr != null) {
      activity.addEventListener(Listener.EVENTNAME_START, new VariableInitializeWithExpression(scriptingEngines, propertyName, srcExpr, "juel"));
    }

    String dst = propertyElement.attributeNS(BpmnParser.BPMN_EXTENSIONS_NS, "dst");
    if (dst != null) {
      activity.addEventListener(Listener.EVENTNAME_END, new VariableDestroyWithVariable(propertyName, dst));
    }

    String destExpr = propertyElement.attributeNS(BpmnParser.BPMN_EXTENSIONS_NS, "dstExpr");
    if (destExpr != null) {
      activity.addEventListener(Listener.EVENTNAME_END, new VariableDestroyWithExpression(scriptingEngines, propertyName, destExpr, "juel"));
    }

    String link = propertyElement.attributeNS(BpmnParser.BPMN_EXTENSIONS_NS, "link");
    if (link != null) {
      activity.addEventListener(Listener.EVENTNAME_START, new VariableInitializeWithVariable(link, propertyName));
      activity.addEventListener(Listener.EVENTNAME_END, new VariableDestroyWithVariable(propertyName, link));
    }

    String linkExpr = propertyElement.attributeNS(BpmnParser.BPMN_EXTENSIONS_NS, "linkExpr");
    if (linkExpr != null) {
      expressionManager.createValueExpression(linkExpr);
      activity.addEventListener(Listener.EVENTNAME_START, new VariableInitializeWithExpression(scriptingEngines, linkExpr, propertyName, "juel"));
      activity.addEventListener(Listener.EVENTNAME_END, new VariableDestroyWithExpression(scriptingEngines, propertyName, linkExpr, "juel"));
    }
  }

  /**
   * Parses all sequence flow of a scope.
   * 
   * @param processElement
   *          The 'process' element wherein the sequence flow are defined.
   * @param scopeElement
   *          The scope to which the sequence flow must be added.
   */
  public void parseSequenceFlow(Element processElement, ScopeElementImpl scopeElement) {
    for (Element sequenceFlowElement : processElement.elements("sequenceFlow")) {

      String id = sequenceFlowElement.attribute("id");
      String sourceRef = sequenceFlowElement.attribute("sourceRef");
      String destinationRef = sequenceFlowElement.attribute("targetRef");
      
      // Implicit check: sequence flow cannot cross (sub) process boundaries: we don't do a processDefinition.findActivity here
      ActivityImpl sourceActivity = scopeElement.findActivity(sourceRef);
      ActivityImpl destinationActivity = scopeElement.findActivity(destinationRef);

      if (sourceActivity == null) {
        throw new ActivitiException("Invalid source of sequence flow '" + id + "'");
      }
      if (destinationActivity == null) {
        throw new ActivitiException("Invalid destination of sequence flow '" + id + "'");
      }

      TransitionImpl transition = sourceActivity.createTransition();
      transition.setId(id);
      transition.setName(sequenceFlowElement.attribute("name"));
      transition.setDestination(destinationActivity);
      parseSequenceFlowConditionExpression(sequenceFlowElement, transition);
    }
  }

  /**
   * Parses a condition expression on a sequence flow.
   * 
   * @param seqFlowElement
   *          The 'sequenceFlow' element that can contain a condition.
   * @param seqFlow
   *          The sequenceFlow object representation to which the condition must
   *          be added.
   */
  public void parseSequenceFlowConditionExpression(Element seqFlowElement, TransitionImpl seqFlow) {
    Element conditionExprElement = seqFlowElement.element("conditionExpression");
    if (conditionExprElement != null) {
      String expr = conditionExprElement.getText().trim();
      String type = conditionExprElement.attributeNS(BpmnParser.XSI_NS, "type");
      if (type != null && !type.equals("tFormalExpression")) {
        throw new ActivitiException("Invalid type on conditionExpression (" + conditionExprElement.getLine() + "). "
                + "Only tFormalExpression is currently supported");
      }

      String language = conditionExprElement.attribute("language");
      if (language == null) {
        language = ExpressionManager.DEFAULT_EXPRESSION_LANGUAGE;
      }

      Condition condition = null;
      if ("uel-value".equals(language)) {
        condition = new UelValueExpressionCondition(expressionManager.createValueExpression(expr));
      } else if ("uel-method".equals(language)) {
        condition = new UelMethodExpressionCondition(expressionManager.createMethodExpression(expr));
      } else {
        throw new ActivitiException("Unknown language for condition: " + language);
      }
      seqFlow.setCondition(condition);
    }
  }

  /**
   * Retrieves the {@link Operation} corresponding with the given operation
   * identifier.
   */
  public Operation getOperation(String operationId) {
    return operations.get(operationId);
  }

  /* Getters, setters and Parser overriden operations */

  public List<ProcessDefinitionImpl> getProcessDefinitions() {
    return processDefinitions;
  }

  public BpmnParse processDefinitionClass(Class< ? extends ProcessDefinition> processDefinitionClass) {
    this.processDefinitionClass = processDefinitionClass;
    return this;
  }

  public BpmnParse name(String name) {
    super.name(name);
    return this;
  }

  public BpmnParse sourceInputStream(InputStream inputStream) {
    super.sourceInputStream(inputStream);
    return this;
  }

  public BpmnParse sourceResource(String resource, ClassLoader classLoader) {
    super.sourceResource(resource, classLoader);
    return this;
  }

  public BpmnParse sourceResource(String resource) {
    super.sourceResource(resource);
    return this;
  }

  public BpmnParse sourceString(String string) {
    super.sourceString(string);
    return this;
  }

  public BpmnParse sourceUrl(String url) {
    super.sourceUrl(url);
    return this;
  }

  public BpmnParse sourceUrl(URL url) {
    super.sourceUrl(url);
    return this;
  }
}
