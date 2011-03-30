package org.activiti.cycle.impl.action.fom;

import java.util.Map;
import java.util.Set;

import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleContext;
import org.activiti.cycle.context.CycleRequestContext;
import org.activiti.cycle.context.CycleSessionContext;
import org.activiti.engine.delegate.VariableScope;

/**
 * Custom {@link VariableScope} for resolving variables using the
 * {@link CycleContext}-hierarchy.
 * 
 * @author daniel.meyer@camunda.com
 */
public class CycleContextVariableScope implements VariableScope {

  public Map<String, Object> getVariables() {
    throw new RuntimeException("not implemented");
  }

  public Map<String, Object> getVariablesLocal() {
    throw new RuntimeException("not implemented");
  }

  public Object getVariable(String variableName) {
    Object obj = CycleRequestContext.get(variableName);
    if (obj != null) {
      return obj;
    }
    obj = CycleSessionContext.get(variableName);
    if (obj != null) {
      return obj;
    }
    obj = CycleApplicationContext.get(variableName);
    return obj;
  }

  public Object getVariableLocal(Object variableName) {
    throw new RuntimeException("not implemented");
  }

  public Set<String> getVariableNames() {
    throw new RuntimeException("not implemented");
  }

  public Set<String> getVariableNamesLocal() {
    throw new RuntimeException("not implemented");
  }

  public void setVariable(String variableName, Object value) {
    throw new RuntimeException("not implemented");
  }

  public Object setVariableLocal(String variableName, Object value) {
    throw new RuntimeException("not implemented");
  }

  public void setVariables(Map<String, ? extends Object> variables) {
    throw new RuntimeException("not implemented");
  }

  public void setVariablesLocal(Map<String, ? extends Object> variables) {
    throw new RuntimeException("not implemented");
  }

  public boolean hasVariables() {
    throw new RuntimeException("not implemented");
  }

  public boolean hasVariablesLocal() {
    throw new RuntimeException("not implemented");
  }

  public boolean hasVariable(String variableName) {
    return getVariable(variableName) != null;
  }

  public boolean hasVariableLocal(String variableName) {
    throw new RuntimeException("not implemented");
  }

  public void createVariableLocal(String variableName, Object value) {
    throw new RuntimeException("not implemented");
  }

  public void createVariablesLocal(Map<String, ? extends Object> variables) {
    throw new RuntimeException("not implemented");
  }

  public void removeVariable(String variableName) {
    throw new RuntimeException("not implemented");
  }

  public void removeVariableLocal(String variableName) {
    throw new RuntimeException("not implemented");
  }

  public void removeVariables() {
    throw new RuntimeException("not implemented");
  }

  public void removeVariablesLocal() {
    throw new RuntimeException("not implemented");
  }

}
