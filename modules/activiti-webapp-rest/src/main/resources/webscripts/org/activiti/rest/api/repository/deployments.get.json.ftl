<#import "../activiti.lib.ftl" as activitiLib>
<#import "repository.lib.ftl" as deploymentLib>
{
  "data": <@deploymentLib.printDeploymentList deployments/>,
  <@activitiLib.printPagination/>
}
