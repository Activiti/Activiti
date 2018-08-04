package org.activiti.spring.security.policies;

import org.activiti.runtime.api.model.payloads.GetProcessDefinitionsPayload;
import org.activiti.runtime.api.model.payloads.GetProcessInstancesPayload;

public interface ProcessSecurityPoliciesManager extends SecurityPoliciesManager {

    GetProcessDefinitionsPayload restrictProcessDefQuery(SecurityPolicyAccess securityPolicyAccess);

    GetProcessInstancesPayload restrictProcessInstQuery(SecurityPolicyAccess securityPolicyAccess);
}
