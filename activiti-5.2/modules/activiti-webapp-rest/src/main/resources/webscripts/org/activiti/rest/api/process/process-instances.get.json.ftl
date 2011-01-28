<#import "../activiti.lib.ftl" as activitiLib>
<#import "process.lib.ftl" as processLib>
{
  "data": <@processLib.printProcessInstanceList processInstances/>,
  <@activitiLib.printPagination/>
}
