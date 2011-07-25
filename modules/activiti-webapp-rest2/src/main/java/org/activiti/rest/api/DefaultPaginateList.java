package org.activiti.rest.api;

import java.util.List;

public class DefaultPaginateList extends AbstractPaginateList {

  @SuppressWarnings("rawtypes")
  @Override
  protected List processList(List list) {
    return list;
  }
}
