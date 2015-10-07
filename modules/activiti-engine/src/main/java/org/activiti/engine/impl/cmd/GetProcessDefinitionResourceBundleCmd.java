package org.activiti.engine.impl.cmd;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionCacheEntry;

public class GetProcessDefinitionResourceBundleCmd implements Command<ResourceBundle>, Serializable {

  private static final long serialVersionUID = -2834025105513923083L;
  
  protected String processDefinitionId;
  protected Locale locale;

  public GetProcessDefinitionResourceBundleCmd(String processDefinitionId, Locale locale) {
    this.processDefinitionId = processDefinitionId;
    this.locale = locale;
  }

  public ResourceBundle execute(CommandContext commandContext) {
    if (processDefinitionId == null) {
      throw new ActivitiIllegalArgumentException("processDefinitionId is null");
    }
    if(locale == null) {
      throw new ActivitiIllegalArgumentException("locale is null");
    }

    ProcessDefinitionCacheEntry processDefinitionCache = commandContext.getProcessEngineConfiguration().getProcessDefinitionCache().get(processDefinitionId);
    return processDefinitionCache.getResourceBundle(locale);
  }
}
