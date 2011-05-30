<#import "../activiti.lib.ftl" as activitiLib>
<#import "process.lib.ftl" as processLib>
{
  "data": <@processLib.printProcessDefinitionList processDefinitions/>,
  <@activitiLib.printPagination/>
}
