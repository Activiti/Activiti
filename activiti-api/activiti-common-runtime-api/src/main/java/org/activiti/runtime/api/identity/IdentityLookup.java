package org.activiti.runtime.api.identity;

import java.util.List;

public interface IdentityLookup {

    List<String> getGroupsForCandidateUser(String userId);

}
