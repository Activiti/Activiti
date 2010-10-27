<#import "../activiti.lib.ftl" as activitiLib>
<#import "task.lib.ftl" as taskLib>
{
  "data": <@taskLib.printTaskList tasks/>,
  <@activitiLib.printPagination/>
}
