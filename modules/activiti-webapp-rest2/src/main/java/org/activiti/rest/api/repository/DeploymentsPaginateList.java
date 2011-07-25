package org.activiti.rest.api.repository;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.repository.Deployment;
import org.activiti.rest.api.AbstractPaginateList;

public class DeploymentsPaginateList extends AbstractPaginateList {

  @SuppressWarnings("rawtypes")
  @Override
  protected List processList(List list) {
    List<DeploymentResponse> responseList = new ArrayList<DeploymentResponse>();
    for (Object deployment : list) {
      responseList.add(new DeploymentResponse((Deployment) deployment));
    }
    return responseList;
  }
}
