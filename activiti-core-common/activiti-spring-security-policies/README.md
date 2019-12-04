# Security Policy Service

Service that can read properties in format:

activiti.security.policies.group.hr.policy.read=SimpleProcess1,SimpleProcess2
activiti.security.policies.user.bob.policy.write=SimpleProcess

Will resolve to find which security policy should be applied to which process definitions. Service does not itself apply the policy - SecurityPolicyApplicationService does that.