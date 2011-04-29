package org.activiti.kickstart.dto;

import org.activiti.kickstart.bpmn20.model.FlowElement;
import org.activiti.kickstart.bpmn20.model.activity.type.ScriptTask;

public class ScriptTaskDto extends BaseTaskDto {

  private String scriptFormat;

  public String getScriptFormat() {
    return scriptFormat;
  }
  public void setScriptFormat(String scriptFormat) {
    this.scriptFormat = scriptFormat;
  }

  private String resultVariableName;

  public String getResultVariableName() {
    return resultVariableName;
  }
  public void setResultVariableName(String resultVariableName) {
    this.resultVariableName = resultVariableName;
  }

  private String script;

  public String getScript() {
    return script;
  }
  public void setScript(String script) {
    this.script = script;
  }

  @Override
  public FlowElement createFlowElement() {
    ScriptTask task = new ScriptTask();
    task.setScriptFormat(getScriptFormat());
    task.setScript(getScript());
    task.setResultVariableName(getResultVariableName());
    return task;
  }
}
