package org.activiti.core.common.spring.security.policies;

import java.util.Map;
import java.util.Set;

public interface SecurityPoliciesManager {

    boolean canRead(String processDefinitionKey,
                    String serviceName);

    boolean canWrite(String processDefinitionKey,
                     String serviceName);

    boolean canRead(String processDefinitionKey);

    boolean canWrite(String processDefinitionKey);

    boolean arePoliciesDefined();

    Map<String, Set<String>> getAllowedKeys(SecurityPolicyAccess... securityPolicyAccess);

}
