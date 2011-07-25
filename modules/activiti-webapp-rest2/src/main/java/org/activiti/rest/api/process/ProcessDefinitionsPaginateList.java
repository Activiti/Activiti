package org.activiti.rest.api.process;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.form.StartFormData;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.rest.api.AbstractPaginateList;
import org.activiti.rest.api.ActivitiUtil;

public class ProcessDefinitionsPaginateList extends AbstractPaginateList {

  @SuppressWarnings("rawtypes")
  @Override
  protected List processList(List list) {
    List<ProcessDefinitionResponse> responseProcessDefinitions = new ArrayList<ProcessDefinitionResponse>();
    for (Object definition : list) {
      ProcessDefinitionResponse processDefinition = new ProcessDefinitionResponse((ProcessDefinitionEntity) definition);
      StartFormData startFormData = ActivitiUtil.getFormService().getStartFormData(((ProcessDefinitionEntity) definition).getId());
      if (startFormData != null) {
        processDefinition.setStartFormResourceKey(startFormData.getFormKey());
      }
      
      processDefinition.setGraphicNotationDefined(isGraphicNotationDefined(((ProcessDefinitionEntity) definition).getId()));
      responseProcessDefinitions.add(processDefinition);
    }
    return responseProcessDefinitions;
  }
  
  private boolean isGraphicNotationDefined(String id) {
    try {
      return ((ProcessDefinitionEntity) ((RepositoryServiceImpl) ActivitiUtil.getRepositoryService())
          .getDeployedProcessDefinition(id)).isGraphicalNotationDefined();
    } catch (Exception e) {
      //Process not deployed?
    }
    return false;
  }
}
