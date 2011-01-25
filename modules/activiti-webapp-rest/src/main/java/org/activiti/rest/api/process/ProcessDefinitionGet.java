package org.activiti.rest.api.process;

import java.util.Map;

import org.activiti.engine.form.StartFormData;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.repository.ProcessDefinitionEntity;
import org.activiti.rest.model.RestProcessDefinition;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

/**
 * Returns details about a process definition.
 *
 * @author Erik Winlof
 */
public class ProcessDefinitionGet extends ActivitiWebScript {

  /**
   * Returns details about a process definition.
   *
   * @param req    The webscripts request
   * @param status The webscripts status
   * @param cache  The webscript cache
   * @param model  The webscripts template model
   */
  @Override
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    String processDefinitionId = req.getMandatoryPathParameter("processDefinitionId");
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) getRepositoryService().createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();

    RestProcessDefinition restProcessDefinition = new RestProcessDefinition(processDefinition);
    StartFormData startFormData = getFormService().getStartFormData(processDefinitionId);
    if (startFormData != null) {
      restProcessDefinition.setStartFormResourceKey(startFormData.getFormKey());
      restProcessDefinition.setGraphicNotationDefined(isGraphicNotationDefined(processDefinitionId));
    }
    model.put("processDefinition", restProcessDefinition);
  }

  private boolean isGraphicNotationDefined(String id) {
    try {
      return ((ProcessDefinitionEntity) ((RepositoryServiceImpl) getRepositoryService())
          .getDeployedProcessDefinition(id)).isGraphicalNotationDefined();
    } catch (Exception e) {
      //Process not deployed?
    }
    return false;
  }

}
