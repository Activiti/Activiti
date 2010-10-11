<#import "../activiti.lib.ftl" as activitiLib>
<#import "repository.lib.ftl" as deploymentLib>
<#escape x as jsonUtils.encodeJSONString(x)>
{
  "data": <@deploymentLib.printDeploymentList deployments/>,
  <@activitiLib.printPagination/>
}
</#escape>
