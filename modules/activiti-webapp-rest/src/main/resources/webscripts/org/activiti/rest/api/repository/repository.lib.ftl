<#macro printDeploymentList deploymentList>
[
  <#list deployments as deployment>
  {
    <@printDeployment deployment/>
  }<#if deployment_has_next>,</#if>
  </#list>
]
</#macro>

<#macro printDeployment deployment>
  "id": "${deployment.id}",
  "name": "${deployment.name}",
  "deploymentTime": <#if deployment.deploymentTime??>"${iso8601Date(deployment.deploymentTime)}"<#else>null</#if>
</#macro>
