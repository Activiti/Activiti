package org.activiti.engine.impl.persistence.entity.json;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.VariableScopeImpl;
import org.activiti.engine.impl.variable.JsonType;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.Map;

/**
 * @author Tim Stephenson.
 */
public abstract class JsonVariableScopeImpl extends VariableScopeImpl {
  @Override
  protected void setVariable(String variableName, Object value, ExecutionEntity sourceActivityExecution, boolean fetchAllVariables) {
    if (JsonType.appearsToBeJson(value)) {
      super.setVariable(variableName, JsonType.parse((String) value), sourceActivityExecution, fetchAllVariables);
    } else {
      super.setVariable(variableName, value, sourceActivityExecution, fetchAllVariables);
    }
  }

  @Override
  public Object setVariableLocal(String variableName, Object value, ExecutionEntity sourceActivityExecution, boolean fetchAllVariables) {
    if (JsonType.appearsToBeJson(value)) {
      return super.setVariableLocal(variableName, JsonType.parse((String) value), sourceActivityExecution, fetchAllVariables);
    } else {
      return super.setVariableLocal(variableName, value, sourceActivityExecution, fetchAllVariables);
    }
  }

}
