package org.activiti.rest.api.identity;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.identity.User;
import org.activiti.rest.api.AbstractPaginateList;

public class GroupUsersPaginateList extends AbstractPaginateList {

  @SuppressWarnings("rawtypes")
  @Override
  protected List processList(List list) {
    List<UserResponse> responseList = new ArrayList<UserResponse>();
    for (Object definition : list) {
      UserResponse response = new UserResponse((User) definition);
      responseList.add(response);
    }
    return responseList;
  }
}
