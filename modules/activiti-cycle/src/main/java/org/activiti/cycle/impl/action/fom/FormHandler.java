package org.activiti.cycle.impl.action.fom;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;

import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContext;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.el.ExpressionManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Reads and parses an action form. This is a very naive implementatation: we
 * search the form for expressions that match the patters ${.*} and evauate them
 * using the engine-provided {@link ExpressionManager}. For the evauation we
 * provide a custom {@link VariableScope}, resolving variables using the
 * {@link CycleContext}-hierarchy (see also {@link CycleContextType}).
 * 
 * This allows action forms to reference bean properties of
 * {@link CycleComponent}s.
 * 
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class FormHandler {

  /** use the activiti expression manager to evaluate expressions */
  private ExpressionManager expressionManager = new ExpressionManager();

  private VariableScope cycleContextVariableScope = new CycleContextVariableScope();

  /**
   * returns a map, mapping form elements to expressions. The map contains an
   * entry for each 'input'-element of a form, such that the type of the element
   * is 'text' and the value attribute contains an expression.
   */
  protected Map<String, String> getExpressions(String form) {

    // parse the form:
    Document document;
    try {
      document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(form.getBytes()));
    } catch (SAXException e) {
      throw new RuntimeException("Could not parse form '" + form + "', not vaild XML.", e);
    } catch (Exception e) {
      throw new RuntimeException("Could not parse form '" + form + "'", e);
    }

    Map<String, String> expressionMap = new HashMap<String, String>();

    NodeList inputElements = document.getElementsByTagName("input");
    getExpressions(inputElements, expressionMap);
    NodeList textAreaElements = document.getElementsByTagName("textarea");
    getExpressions(textAreaElements, expressionMap);
    return expressionMap;
  }

  protected void getExpressions(NodeList nodeList, Map<String, String> expressionMap) {

    for (int i = 0; i < nodeList.getLength(); i++) {
      Node currentNode = nodeList.item(i);
      if (!(currentNode instanceof Element)) {
        continue;
      }
      Element currentElement = (Element) currentNode;

      String nameAttribute = null;
      String valueAttribute = null;
      // only look at 'text' elements
      if ("input".equals(currentElement.getTagName())) {
        if ("text".equals(currentElement.getAttribute("type"))) {
          nameAttribute = currentElement.getAttribute("name");
          valueAttribute = currentElement.getAttribute("value");
        } else if ("checkbox".equals(currentElement.getAttribute("type"))) {
          nameAttribute = currentElement.getAttribute("name");
          valueAttribute = currentElement.getAttribute("property");
        }
      }
      if ("checkbox".equals(currentElement.getTagName())) {
        nameAttribute = currentElement.getAttribute("name");
        valueAttribute = currentElement.getAttribute("property");
      }

      if (nameAttribute == null || nameAttribute.length() == 0 || valueAttribute == null) {
        continue;
      }

      if (!valueAttribute.matches("\\$\\{.*\\}")) {
        continue;
      }

      if (expressionMap.containsKey(nameAttribute)) {
        throw new RuntimeException("Form contains a property with name '" + nameAttribute + "' more than once.");
      }
      expressionMap.put(nameAttribute, valueAttribute);
    }
  }

  protected Map<String, Object> getValues(Collection<String> expressions) {

    Map<String, Object> values = new HashMap<String, Object>();

    for (String expression : expressions) {
      // check whether we have already resolved this expression
      if (values.containsKey(expression)) {
        continue;
      }
      // evaluate the expression (might throw an exception (which is OK))
      Object value = expressionManager.createExpression(expression).getValue(cycleContextVariableScope);
      if (value == null) {
        value = "";
      }
      values.put(expression, value.toString());
    }
    return values;
  }

  public String parseForm(String form) {

    Map<String, String> expressions = getExpressions(form);
    Map<String, Object> valueMap = getValues(expressions.values());

    for (String expression : valueMap.keySet()) {
      String expr = expression;
      if (expr.contains("${")) {
        expr = expr.substring(2); // remove '${
        expr = expr.substring(0, expr.length() - 1);
        expr = "\\$\\{" + expr + "\\}";
      }
      // TODO: handle different Types (at the moment we only support strings)
      form = form.replaceAll(expr, valueMap.get(expression).toString());
    }

    return form;
  }

  public void setValues(String form, Map<String, Object> parameters) {

    Map<String, String> expressions = getExpressions(form);
    for (Entry<String, String> expressionEntry : expressions.entrySet()) {
      Object value = parameters.get(expressionEntry.getKey());
      Expression expression = expressionManager.createExpression(expressionEntry.getValue());
      expression.setValue(value, cycleContextVariableScope);
    }
  }
}
