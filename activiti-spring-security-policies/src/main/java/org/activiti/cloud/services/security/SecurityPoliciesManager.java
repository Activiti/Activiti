package org.activiti.cloud.services.security;

public interface SecurityPoliciesManager {

    boolean canRead(String processDefinitionKey,
                    String appName);

    boolean canWrite(String processDefinitionKey,
                     String appName);
}
