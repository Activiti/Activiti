package org.activiti.core.common.spring.security.policies;

import org.activiti.api.process.model.payloads.GetProcessDefinitionsPayload;
import org.activiti.api.process.model.payloads.GetProcessInstancesPayload;

public interface ProcessSecurityPoliciesManager extends SecurityPoliciesManager {

    GetProcessDefinitionsPayload restrictProcessDefQuery(SecurityPolicyAccess securityPolicyAccess);

    GetProcessInstancesPayload restrictProcessInstQuery(SecurityPolicyAccess securityPolicyAccess);
}
