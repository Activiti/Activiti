<#import "../activiti.lib.ftl" as activitiLib>
<#import "task.lib.ftl" as taskLib>
<#escape x as jsonUtils.encodeJSONString(x)>
{
  "data": <@taskLib.printTaskList tasks/>,
  <@activitiLib.printPagination/>
}
</#escape>