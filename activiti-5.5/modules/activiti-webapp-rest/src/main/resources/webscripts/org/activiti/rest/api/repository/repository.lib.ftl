<#escape x as jsonUtils.encodeJSONString(x)>

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
  "id": <#if deployment.id??>"${deployment.id}"<#else>null</#if>,
  "name": <#if deployment.name??>"${deployment.name}"<#else>null</#if>,
  "deploymentTime": <#if deployment.deploymentTime??>"${iso8601Date(deployment.deploymentTime)}"<#else>null</#if>
</#macro>

</#escape>
