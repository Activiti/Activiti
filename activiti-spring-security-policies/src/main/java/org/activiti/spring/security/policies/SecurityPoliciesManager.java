package org.activiti.spring.security.policies;

import org.activiti.runtime.api.model.payloads.GetProcessDefinitionsPayload;
import org.activiti.runtime.api.model.payloads.GetProcessInstancesPayload;

/* is this too process specific? */
public interface SecurityPoliciesManager {

    boolean canRead(String processDefinitionKey,
                    String appName);

    boolean canWrite(String processDefinitionKey,
                     String appName);

    boolean canRead(String processDefinitionKey);

    boolean canWrite(String processDefinitionKey);

    /* Find the right level for this methods */

    GetProcessDefinitionsPayload restrictProcessDefQuery(SecurityPolicy securityPolicy);

    GetProcessInstancesPayload restrictProcessInstQuery(SecurityPolicy securityPolicy);

}
