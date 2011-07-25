package org.activiti.rest.api.process;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.rest.api.AbstractPaginateList;

public class ProcessInstancesPaginateList extends AbstractPaginateList {

  @SuppressWarnings("rawtypes")
  @Override
  protected List processList(List list) {
    List<ProcessInstancesResponse> processResponseList = new ArrayList<ProcessInstancesResponse>();
    for (Object instance : list) {
      processResponseList.add(new ProcessInstancesResponse(
          (HistoricProcessInstance) instance));
    }
    return processResponseList;
  }
}
