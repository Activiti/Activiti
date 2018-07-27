package org.activiti.runtime.api.identity;

import java.util.List;

public interface ActivitiUser {

    String getUsername();

    String getPassword();

    List<String> getGroupIds();

    List<String> getRoles();
}
