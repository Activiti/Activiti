package org.activiti.rest.api.management;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.runtime.Job;
import org.activiti.rest.api.AbstractPaginateList;

public class JobsPaginateList extends AbstractPaginateList {

  @SuppressWarnings("rawtypes")
  @Override
  protected List processList(List list) {
    List<JobResponse> responseList = new ArrayList<JobResponse>();
    for (Object job : list) {
      responseList.add(new JobResponse((Job) job));
    }
    return responseList;
  }
}
